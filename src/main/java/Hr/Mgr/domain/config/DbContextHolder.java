package Hr.Mgr.domain.config;

import java.util.Objects;

public class DbContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void useMasterWrite() {
        setContext("master-write");
    }
    public static void useMasterRead() {
        setContext("master-read");
    }
    public static void useReplicaWrite() {
        setContext("replica-write");
    }
    public static void useReplicaRead() {
        setContext("replica-read");
    }

    public static void setContext(String newContext) {
        CONTEXT.set(newContext);
//         print();
    }
    public static String getCurrentDb() { return CONTEXT.get(); }

    public static void clear() {
        //CONTEXT.remove();
    }

    public static void print(){
        System.out.println("cur : "+ CONTEXT.get());
    }
}
