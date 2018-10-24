package com.soaer.mqtt;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Sunny on 2018/10/24.
 */
public class Mqtt {
    private MqttAsyncClient mqttAsyncClient;
    private MqttConnectOptions connectionOptions;
    private IMqttActionListener mqttMessageListener;
    private static final String TLS_V_1_2 = "TLSv1.2";

    public Mqtt(String serverURI, String clientId, String userName, String
            password, SocketFactory socketFactory, MqttCallback mqttCallbackListener,
                          IMqttActionListener mqttMessageListener) throws MqttException {

        if (serverURI == null || mqttCallbackListener == null || mqttMessageListener == null) {
            throw new IllegalArgumentException("serverURI, mqttCallbackListener, mqttMessageListener can't be null!");
        }
        this.mqttAsyncClient = new MqttAsyncClient(serverURI, clientId, new MemoryPersistence());
        this.mqttAsyncClient.setManualAcks(true);
        this.connectionOptions = new MqttConnectOptions();
        this.initOptions(userName, password, socketFactory);
        this.mqttMessageListener = mqttMessageListener;
        this.mqttAsyncClient.setCallback(mqttCallbackListener);
    }

    private void initOptions(String userName, String password, SocketFactory socketFactory) {
        this.connectionOptions.setKeepAliveInterval(180);
        this.connectionOptions.setCleanSession(true);
        this.connectionOptions.setSocketFactory(socketFactory);
        if (password != null && !"".equals(password)) {
            this.connectionOptions.setPassword(password.toCharArray());
        }
    }

    public MqttAsyncClient getMqttAsyncClient() {
        return this.mqttAsyncClient;
    }

    /**
     * 是否连接成功
     * @return false  不能发送和订阅主题消息。
     */
    public boolean isConnected() {
        if (null != this.mqttAsyncClient) {
            return this.mqttAsyncClient.isConnected();
        }
        return false;
    }

    public IMqttToken disconnect() throws MqttException {
        if (this.mqttAsyncClient != null) {
            return this.mqttAsyncClient.disconnect();
        }
        return null;
    }

    public void close() throws MqttException {
        if (this.mqttAsyncClient != null) {
            this.mqttAsyncClient.close();
        }
    }

    public void openConnection() {
        try {
            this.mqttAsyncClient.connect(connectionOptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布消息到主题
     *
     * @param message
     */
    public void publishMessage(Message message) {
        String topic = message.getTopic();
        MqttMessage mqttMessage = new MqttMessage(message.getPayload());
        mqttMessage.setQos(message.getQos());

        try {
            this.mqttAsyncClient.publish(topic, mqttMessage, message, mqttMessageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题
     *
     * @param message
     */
    public void subscribeTopic(Message message) {
        try {
            this.mqttAsyncClient.subscribe(message.getTopic(), message.getQos(), message, mqttMessageListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribeTopic(Message message) {
        try {
            this.mqttAsyncClient.unsubscribe(message.getTopic(), message, mqttMessageListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param keystore
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws CertificateException
     */
    public static SSLSocketFactory getFactory(KeyStore keystore) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (null == keystore) {
                tmf.init((KeyStore) null);
            } else {
                tmf.init(keystore);
            }
            SSLContext context = SSLContext.getInstance(TLS_V_1_2);
            context.init(null, tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 双向验证获取SSLSocketFactory
     *
     * @param caKeystore       存放CA证书的Keystore， @See Junit Test
     * @param clientKeystore   存放客户端的证
     * @param keystorePassword clientKeystore 的密码
     *
     * @return
     */
    public static SSLSocketFactory getFactory(KeyStore caKeystore, KeyStore clientKeystore, String keystorePassword) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKeystore);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientKeystore, keystorePassword.toCharArray());
            SSLContext context = SSLContext.getInstance(TLS_V_1_2);
            KeyManager[] kms = kmf.getKeyManagers();
            context.init(kms, tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }
}
