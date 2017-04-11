package mase.vrep;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;

public class VRepComm {

    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_PORT = 25000;
    public static final remoteApi VREP_API = new remoteApi();
    private static final VRepClient DEF_CLIENT = new VRepClient(DEFAULT_IP, DEFAULT_PORT);
    
    public static synchronized VRepClient getDefaultContainer(boolean forceNew) {
        if(forceNew || !DEF_CLIENT.isAvailable()) {
            terminateAll(); // just in case, close all opened connections
            DEF_CLIENT.init();
        }
        return DEF_CLIENT;
    }
    
    public static synchronized void terminateAll() {
        VREP_API.simxFinish(-1); 
    }

    public static class VRepClient {

        final String ip;
        final int port;
        int clientId;
        int faults = 0;

        public VRepClient(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.faults = 0;
            this.clientId = -1;
        }

        public synchronized boolean init() {
            if(clientId != -1) { // restarting
                terminateClient();
            }
            System.err.println("[VREP] Trying to connect to " + this);
            this.clientId = VREP_API.simxStart(ip, port, true, false, 5000, 5);
            if (clientId == -1) {
                System.err.println("[VREP] Not connected! " + this);
                this.faults++;
            } else {
                System.err.println("[VREP] Connected client " + this);
                VREP_API.simxClearStringSignal(clientId, "toClient", remoteApi.simx_opmode_oneshot_wait);
                VREP_API.simxClearStringSignal(clientId, "fromClient", remoteApi.simx_opmode_oneshot_wait);
                this.faults = 0;
            }
            return clientId != -1;
        }

        public synchronized boolean isAvailable() {
            return clientId != -1;
        }

        public synchronized void terminateClient() {
            if(clientId != -1) {
                System.err.println("[VREP] Terminating client " + this);
                VREP_API.simxFinish(clientId);
                clientId = -1;
            }
        }

        public synchronized float[] getDataFromVREP() {
            if(!isAvailable()) {
                return null;
            }
            CharWA str = new CharWA(0);
            int signalVal = VREP_API.simxGetStringSignal(clientId, "toClient", str, remoteApi.simx_opmode_oneshot_wait);

            while (signalVal != remoteApi.simx_return_ok) {
                if (signalVal == 3 || signalVal == remoteApi.simx_return_initialize_error_flag) {
                    // error in the connection
                    System.err.println("[VREP] Fatal error receiving data from " + this + ": " + signalVal);
                    this.faults++;
                    terminateClient();
                    return null;
                }
                try {
                    // waiting for the results to come
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                signalVal = VREP_API.simxGetStringSignal(clientId, "toClient", str, remoteApi.simx_opmode_oneshot_wait);
            }

            VREP_API.simxClearStringSignal(clientId, "toClient", remoteApi.simx_opmode_oneshot_wait);
            this.faults = 0;
            
            FloatWA f = new FloatWA(0);
            f.initArrayFromCharArray(str.getArray());
            float[] array = f.getArray();
            System.err.println("[VREP] Received from client " + this + ": " + (array == null ? "NULL" : array.length));
            return array;
        }

        public synchronized boolean sendDataToVREP(float[] arr) {
            if(!isAvailable()) {
                return false;
            }            
            FloatWA f = new FloatWA(arr.length);
            System.arraycopy(arr, 0, f.getArray(), 0, arr.length);
            char[] chars = f.getCharArrayFromArray();
            String tempStr = new String(chars);
            CharWA str = new CharWA(tempStr);
            System.err.println("[VREP] Sending to client " + this + ": " + arr.length);
            int status = VREP_API.simxWriteStringStream(clientId, "fromClient", str, remoteApi.simx_opmode_oneshot_wait);
            if (status != remoteApi.simx_return_ok) {
                System.err.println("[VREP] Error sending data to " + this + ": " + status);
                this.faults++;
                terminateClient();
            } else {
                this.faults = 0;
            }
            return status == remoteApi.simx_return_ok;
        }

        @Override
        public String toString() {
            return clientId + "@" + ip + ":" + port;
        }
    }
}
