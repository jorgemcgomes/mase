    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.EvolutionState;
import ec.util.Parameter;
import java.awt.Color;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 *
 * @author jorge
 */
public class ParamUtils {

    public static final String SEPARATOR_2D = "[:\\-]+";
    public static final String SEPARATOR_ARRAY = "[,;]+";
    public static final int MAX_COUNT = 20;

    /**
     * Use this annotation in the parameters that need to be automatically
     * filled. Parameters MUST BE PUBLIC.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Param {

        String name() default "";
    }

    /**
     * Use this annotation in parameters that are defined multiple times for
     * different robots. Example:
     *
     * @RobotParam problem.robot.radius = 1 --> Default value
     * problem.robot.0.radius = 2 --> Override problem.robot.1.radius = 3 -->
     * Override The attribute MUST be an array to store the different values for
     * different robots
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MultiParam {

        String name() default ""; // uses the attribute name

        String base() default "robot";

        int count() default 0; // uses up to the MAX_COUNT
    }

    /**
     * Force a parameter to be ignored during the auto-fill
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreParam {
    }

    /**
     * Search the ParameterDatabase to automatically fill the object's fields.
     *
     * @param params The object to search for fields to fill. Only fields that
     * are PUBLIC and ANNOTATED with the @Param annotation are attempted.
     * @param db The parameter database where the values are going to be
     * fetched.
     * @param base The base parameter.
     * @param defaultBase The default base parameter.
     * @param useAll If true, the attributes dont need to have the annotation
     */
    public static void autoSetParameters(Object params, EvolutionState state, Parameter base, Parameter defaultBase, boolean useAll) {
        Field[] fields = params.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                // Check for attribute visibility and skip if its private
                if (Modifier.isPrivate(field.getModifiers())) {
                    System.out.println("- " + field.getName() + ": IGNORED (PRIVATE)");
                    continue;
                }
                field.setAccessible(true);
                
                // Ignore attribute it is static
                if (Modifier.isStatic(field.getModifiers())) {
                    System.out.println("- " + field.getName() + ": IGNORED (STATIC). Using default: " + field.get(params));
                    continue;
                }
                
                // Ignore attribute if it has the @IgnoreParam annotation
                if (field.isAnnotationPresent(IgnoreParam.class)) {
                    System.out.println("- " + field.getName() + ": IGNORED (ANNOTATION). Using default: " + field.get(params));
                    continue;
                }
                
                // It is a composite param, having the @MultiParam annotation
                if (field.isAnnotationPresent(MultiParam.class)) {
                    MultiParam annotation = field.getAnnotation(MultiParam.class);
                    Class<?> type = field.getType();
                    if (type.isArray()) {
                        String name = annotation.name().equals("") ? field.getName() : annotation.name();
                        Parameter defaultRobot = base.push(annotation.base()).push(name);
                        if(!state.parameters.exists(defaultRobot, null)) {
                            defaultRobot = defaultBase.push(annotation.base()).push(name);
                        }
                        Object array = Array.newInstance(type.getComponentType(), MAX_COUNT);
                        for (int i = 0; i < (annotation.count() != 0 ? annotation.count() : MAX_COUNT); i++) {
                            Parameter paramRobot = base.push(annotation.base()).push(i + "").push(name);
                            if(!state.parameters.exists(paramRobot, null)) {
                                paramRobot = defaultBase.push(annotation.base()).push(i + "").push(name);
                            }
                            if (state.parameters.exists(paramRobot, defaultRobot)) {
                                Object value = getValue(type.getComponentType(), state, paramRobot, defaultRobot);
                                Array.set(array, i, value);
                            }
                        }
                        field.set(params, array);
                        System.out.println("V " + field.getName() + ": SET RobotParam: " + Arrays.toString((Object[]) array));
                    } else {
                        System.out.println("X " + field.getName() + ": Field is not an array");
                    }
                } else if (field.isAnnotationPresent(Param.class) || useAll) { // It is a simple attribute
                    Param annotation = field.getAnnotation(Param.class);
                    String name = (annotation == null || annotation.name().equals("")) ? field.getName() : annotation.name();
                    if (state.parameters.exists(base.push(name), defaultBase.push(name))) {
                        Object value = getValue(field.getType(), state, base.push(name), defaultBase.push(name));
                        field.set(params, value);
                        System.out.println("V " + field.getName() + ": SET Param: " + value);
                    } else {
                        System.out.println("X " + field.getName() + ": Parameter not found (" + base.push(name) + " OR " + defaultBase.push(name) + "). Using default: " + field.get(params));
                    }
                }
            } catch (Exception ex) {
                System.out.println("X " + field.getName() + ": FATAL EXCEPTION: " + ex.getMessage());
            }
        }
    }

    public static Object getValue(Class<?> type, EvolutionState state, Parameter baseParam, Parameter defaultBaseParam) throws Exception {
        String val = state.parameters.getString(baseParam, defaultBaseParam);
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            String[] split = val.split(SEPARATOR_ARRAY);
            Object newArray = Array.newInstance(componentType, split.length);
            for (int i = 0; i < split.length; i++) {
                Object parse = parseType(componentType, split[i]);
                Array.set(newArray, i, parse);
            }
            return newArray;
        } else {
            return parseType(type, val);
        }
    }

    public static Object parseType(Class<?> type, String stringVal) throws Exception {
        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return Integer.parseInt(stringVal);
        } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
            return Long.parseLong(stringVal);
        } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
            return Float.parseFloat(stringVal);
        } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
            return Double.parseDouble(stringVal);
        } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(stringVal);
        } else if (type.equals(String.class)) {
            return stringVal;
        } else if (type.isEnum()) {
            Object[] consts = type.getEnumConstants();
            for (Object o : consts) {
                if (o.toString().equalsIgnoreCase(stringVal)) {
                    return o;
                }
            }
            throw new Exception("Unknown constant:" + stringVal);
        } else if (type.equals(File.class)) {
            return new File(stringVal);
        } else if (type.equals(Double2D.class)) {
            if (stringVal.trim().equalsIgnoreCase("null")) {
                return null;
            }
            String[] split = stringVal.split(SEPARATOR_2D);
            if (split.length != 2) {
                throw new Exception("Unexpected split when parsing Double2D: " + stringVal);
            }
            return new Double2D(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        } else if (type.equals(Int2D.class)) {
            if (stringVal.trim().equalsIgnoreCase("null")) {
                return null;
            }
            String[] split = stringVal.split(SEPARATOR_2D);
            if (split.length != 2) {
                throw new Exception("Unexpected split when parsing Int2D: " + stringVal);
            }
            return new Double2D(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } else if (type.equals(Color.class)) {
            return parseColor(stringVal);
        } else {
            throw new Exception("Unknown type: " + type.getName());
        }
    }

     public static Color parseColor(String str) {
        String[] split = str.split("-|\\.|,|;");
        if (split.length == 1) {
            try {
                Field field = Color.class.getField(split[0]);
                return (Color) field.get(null);
            } catch (Exception ex) {
                return Color.decode(split[0]);
            }
        } else if (split.length == 3) {
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        } else if (split.length == 4) {
            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }
        return null;
    }
    
}
