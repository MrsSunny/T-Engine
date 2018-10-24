package com.soaer.mqtt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;
import javax.net.ssl.SSLSocketFactory;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * keytool -importcert -trustcacerts -alias <aliasName> -file <ca>.crt -keystore ca_trust.keystore
 * openssl pkcs12 -export -in <client>.crt -inkey <client>.key -name cli -out <client>.p12
 * keytool -importkeystore -deststorepass <your password> -destkeystore <keystore> -srckeystore <client>.p12
 * -srcstoretype PKCS12
 * <p>
 * mosquitto_sub -h test.mosquitto.org -p 8884 -t "topic111111" --cafile mosquitto.org.crt --cert client.crt --key
 * client.key
 * mosquitto_sub -h test.mosquitto.org -p 8883 -t "topic111111" --cafile mosquitto.org.crt
 *
 *
 * Created by Sunny on 2018/10/24.
 */

public class MqttConnectionTest {

    //无加密信道
    private static String U0;

    //双向加密信道
    private static String U1;

    //单向加密信道
    private static String U2;
    private static String CLIENT_ID;

    //ca证书的keystore地址
    private String caKeystorePath;

    //client证书的keystore地址
    private String clientKeystorePath;

    private static String PASSWORD;

    private static final String TLS_V_1_2 = "TLSv1.2";

    /**
     * 使用公共的mqtt服务器，证书可以在官方网站下载
     */
    @Before
    public void setUp() {
        U0 = "tcp://test.mosquitto.org";
        U1 = "ssl://test.mosquitto.org:8883";
        U2 = "ssl://test.mosquitto.org:8884";
        CLIENT_ID = "BaiduBceMQTTTest";
        URL resPath = this.getClass().getResource("/");
        caKeystorePath = resPath.getPath() + File.separator + "mqtt" + File.separator + "ca.keystore";
        clientKeystorePath = resPath.getPath() + File.separator + "mqtt" + File.separator  + "client.keystore";
        PASSWORD = "11111111";
    }

    /**
     * 单向认证测试
     *
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws MqttException
     */
    @Test
    public void mqttSendTestWithTLS() throws IOException, CertificateException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException, MqttException, InterruptedException {
        SSLSocketFactory factory = Mqtt.getFactory(readCaKeyStore());
        Assert.assertNotNull(factory);
        pubMessage(factory, U1);
    }

    /**
     * 双向认证测试
     *
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws MqttException
     * @throws InterruptedException
     * @throws UnrecoverableKeyException
     */
    @Test
    public void mqttSendTestWithClientTLS()
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, MqttException, InterruptedException, UnrecoverableKeyException {
        SSLSocketFactory factory = Mqtt.getFactory(readCaKeyStore(), readClientKeyStore(), PASSWORD);
        Assert.assertNotNull(factory);
        pubMessage(factory, U2);
    }

    public static class A implements MqttCallback {

        public void connectionLost(Throwable throwable) {

        }

        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

        }

        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }

    public static class B implements IMqttActionListener {

        public void onSuccess(IMqttToken iMqttToken) {

        }

        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

        }
    }

    public void pubMessage(SSLSocketFactory factory, String serverURI) throws MqttException, InterruptedException {
        Mqtt connection = new Mqtt(serverURI, CLIENT_ID + UUID.randomUUID().toString(), null, null,
                factory, new A(), new B());
        connection.openConnection();
        //测试发布需要先判断是否连接上远程MQTT Broker

        int i = 10;
        while (i > 0) {
            if (connection.isConnected()) {
                Message message = new Message();
                message.setPayload("baidu test mqtt message".getBytes());
                message.setQos(1);
                message.setTopic("topic111111");
                connection.publishMessage(message);
                connection.publishMessage(message);
                connection.publishMessage(message);
                connection.publishMessage(message);
                connection.publishMessage(message);
                connection.publishMessage(message);
                break;
            }
            Thread.sleep(2000);
            i--;
        }
    }

    /**
     * 获取ca keystore文件
     *
     * @return
     */
    private KeyStore readCaKeyStore() {
        KeyStore keystore = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(caKeystorePath);
            keystore = KeyStore.getInstance("JKS");
            String keypwd = PASSWORD;
            keystore.load(is, keypwd.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return keystore;
    }

    /**
     * 获取client keystore文件
     *
     * @return
     */
    private KeyStore readClientKeyStore() {
        KeyStore keystore = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(clientKeystorePath);
            keystore = KeyStore.getInstance("jks");
            String keypwd = PASSWORD;
            keystore.load(is, keypwd.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return keystore;
    }
}