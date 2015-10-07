/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.mason;

import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

/**
 *
 * @author jorge
 */
public class ParamUtils {

    /**
     * Use this annotation in the parameters that need to be automatically filled.
     * Parameters MUST BE PUBLIC.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Param {

        String name() default "";
    }

    /**
     * Search the ParameterDatabase to automatically fill the object's fields.
     * @param params The object to search for fields to fill. Only fields that are PUBLIC and ANNOTATED with the @Param annotation are attempted.
     * @param db The parameter database where the values are going to be fetched.
     * @param base The base parameter.
     * @param defaultBase The default base parameter.
     */
    public static void autoSetParameters(Object params, ParameterDatabase db, Parameter base, Parameter defaultBase) {
        Field[] fields = params.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(Param.class)) {
                    Param annotation = field.getAnnotation(Param.class);
                    String name = annotation.name();
                    if (name.equals("")) {
                        name = field.getName();
                    }
                    if (db.exists(base.push(name), defaultBase.push(name))) {
                        Class<?> type = field.getType();
                        Object value = null;
                        if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
                            value = db.getInt(base.push(name), defaultBase.push(name));
                        } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
                            value = db.getLong(base.push(name), defaultBase.push(name));
                        } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
                            value = db.getFloat(base.push(name), defaultBase.push(name));
                        } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
                            value = db.getDouble(base.push(name), defaultBase.push(name));
                        } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
                            value = db.getBoolean(base.push(name), defaultBase.push(name), false);
                        } else if (type.equals(String.class)) {
                            value = db.getString(base.push(name), defaultBase.push(name));
                        } else if (type.equals(File.class)) {
                            value = db.getFile(base.push(name), defaultBase.push(name));
                        }
                        if (value != null) {
                            field.set(params, value);
                        } else {
                            System.out.println("*** Parameter " + name + " with unknown type: " + type.getName());
                        }
                    } else {
                        System.out.println("*** Parameter " + name + " not found: " + base.push(name) + " OR " + defaultBase.push(name));
                        System.out.println("*** Using default value: " + field.get(params));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
