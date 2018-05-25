package com.mmmmar.helmsman;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ClientService extends Service {

    private Op op = new Op();

    @Override
    public IBinder onBind(Intent intent) {
        return op;
    }

    public interface StateListener {
        void onServerConnect();
        void onServerDisconnect();
        void onServerError(Throwable throwable);
        void onServerReply(byte[] data);
    }

    public class Op extends Binder {

        private StateListener stateListener;

        public StateListener getStateListener() {
            return stateListener;
        }

        public void setStateListener(StateListener stateListener) {
            this.stateListener = stateListener;
        }

        public void connect() {

        }

        public void disconnect() {

        }

        public void send(byte[] data) {

        }
    }

}
