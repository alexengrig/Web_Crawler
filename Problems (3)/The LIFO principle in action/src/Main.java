import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final int n = scanner.nextInt();
        final Deque<Integer> stack = new ArrayDeque<>(n);
        for (int i = 0; i < n; i++) {
            final int number = scanner.nextInt();
            stack.push(number);
        }
        stack.forEach(System.out::println);
    }
}