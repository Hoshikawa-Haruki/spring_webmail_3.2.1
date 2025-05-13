/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deu.cse.spring_webmail.model;

import lombok.extern.slf4j.Slf4j;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.*;

@Slf4j
public class UserAdminAgent {

    private String server;
    private int port;
    private String ROOT_ID;
    private String ROOT_PASSWORD;
    private String ADMIN_ID;

    private JMXConnector connector;
    private MBeanServerConnection mbsc;
    private ObjectName userRepositoryMBean;
    
    //상수 선언
    private static final String TYPE_STRING        = "java.lang.String";
    private static final String[] SIG_SINGLE_STRING = { TYPE_STRING }; 
    private static final String[] SIG_DOUBLE_STRING = { TYPE_STRING, TYPE_STRING };

    // 기본 생성자
    public UserAdminAgent() {
    }

    /**
     * 필드 초기화 및 JMX 서버 연결 시도
     */
    public UserAdminAgent(String server, int port, String cwd, String root_id, String root_pass, String admin_id) {
        log.debug("UserAdminAgent JMX init: server = {}, port = {}, adminId = {}", server, port, admin_id);
        this.server = server;
        this.port = port;
        this.ROOT_ID = root_id;
        this.ROOT_PASSWORD = root_pass;
        this.ADMIN_ID = admin_id;

        try {
            connect();
        } catch (Exception e) {
            log.error("JMX 연결 실패: {}", e.getMessage());
        }
    }

    /**
     * JMX 서버에 연결하여 UsersRepository MBean 획득
     */
    private void connect() throws Exception {
        String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", server, port);
        Map<String, Object> environment = new HashMap<>();
        environment.put(JMXConnector.CREDENTIALS, new String[]{ROOT_ID, ROOT_PASSWORD});

        JMXServiceURL serviceURL = new JMXServiceURL(jmxUrl);
        connector = JMXConnectorFactory.connect(serviceURL, environment);
        mbsc = connector.getMBeanServerConnection();
        userRepositoryMBean = new ObjectName("org.apache.james:type=component,name=usersrepository");

        log.info("JMX 연결 성공");
    }

    /**
     * 사용자 추가
     *
     * @param userId 추가할 사용자 ID
     * @param password 추가할 사용자 비밀번호
     * @return 추가 성공 여부
     */
    public boolean addUser(String userId, String password) {
        try {
            mbsc.invoke(
                    userRepositoryMBean,
                    "addUser",
                    new Object[]{userId, password},
                    SIG_DOUBLE_STRING
            );
            log.info("사용자 추가 성공: {}", userId);
            return true;
        } catch (Exception e) {
            log.error("addUser 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 등록된 사용자 목록 조회
     *
     * @return 사용자 ID 목록
     */
    public List<String> getUserList() {
        List<String> users = new LinkedList<>();
        try {
            Object result = mbsc.invoke(userRepositoryMBean, "listAllUsers", null, null);
            if (result instanceof String[]) {
                String[] userArray = (String[]) result;
                for (String user : userArray) {
                    // 관리자 계정은 제외하고 리스트에 추가
                    if (user != null && !user.trim().equalsIgnoreCase(ADMIN_ID.trim())) {
                        users.add(user);
                    }
                }
            }
            log.info("사용자 목록 조회 성공");
        } catch (Exception e) {
            log.error("getUserList 실패: {}", e.getMessage());
        }
        return users;
    }

    /**
     * 선택된 사용자 삭제
     *
     * @param userList 삭제할 사용자 ID 배열
     * @return 삭제 성공 여부
     */
    public boolean deleteUsers(String[] userList) {
        boolean status = false;
        try {
            for (String userId : userList) {
                mbsc.invoke(
                        userRepositoryMBean,
                        "deleteUser",
                        new Object[]{userId},
                        SIG_SINGLE_STRING
                );
                log.info("사용자 삭제됨: {}", userId);
            }
            status = true;
        } catch (Exception e) {
            log.error("deleteUsers 실패: {}", e.getMessage());
        }
        return status;
    }

    /**
     * 사용자 존재 여부 확인
     *
     * @param userId 확인할 사용자 ID
     * @return 존재(true) 여부
     */
    public boolean verify(String userId) {
        try {
            Boolean exists = (Boolean) mbsc.invoke(
                    userRepositoryMBean,
                    "contains",
                    new Object[]{userId},
                    SIG_SINGLE_STRING
            );
            log.info("사용자 존재 여부 확인({}): {}", userId, exists);
            return exists;
        } catch (Exception e) {
            log.error("verify 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JMX 연결 종료
     *
     * @return 종료 성공 여부
     */
    public boolean quit() {
        try {
            if (connector != null) {
                connector.close();
                log.info("JMX 연결 종료");
            }
            return true;
        } catch (Exception e) {
            log.error("quit 실패: {}", e.getMessage());
            return false;
        }
    }
}
