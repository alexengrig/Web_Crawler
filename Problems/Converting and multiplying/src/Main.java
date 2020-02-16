import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            if ("0".equals(s)) {
                break;
            }
            try {
                System.out.println(Integer.parseInt(s) * 10);
            } catch (Exception e) {
                System.out.println("Invalid user input: " + s);
            }
        }
    }
}