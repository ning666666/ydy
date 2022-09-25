import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {
    @org.junit.jupiter.api.Test
    public void testEncoder() {
        String password = "123";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }

    public static void main(String[] args) {
        String password = "123";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
