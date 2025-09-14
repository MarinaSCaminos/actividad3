package ar.edu.unlu.bd2.view;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

/**
 * Utilidad simple para leer datos desde consola con validaciones.
 * Lee SIEMPRE líneas completas y parsea manualmente para evitar problemas de Scanner.
 */
public final class InputReader {

    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    private InputReader() {}

    // ===================== Lecturas base =====================

    public static String nextLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String s = IN.readLine();
                if (s != null) return s;
            } catch (IOException e) {
                System.err.println("Error leyendo entrada: " + e.getMessage());
            }
        }
    }

    public static String nextNonEmpty(String prompt) {
        while (true) {
            String s = nextLine(prompt);
            if (s != null && !s.trim().isEmpty()) return s.trim();
            System.out.println("Valor vacío. Intente nuevamente.");
        }
    }

    public static int nextInt(String prompt) {
        while (true) {
            String s = nextLine(prompt);
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    public static int nextIntInRange(String prompt, int min, int max) {
        while (true) {
            int v = nextInt(prompt);
            if (v < min || v > max) {
                System.out.println("El valor debe estar entre " + min + " y " + max + ".");
            } else {
                return v;
            }
        }
    }

    public static long nextLong(String prompt) {
        while (true) {
            String s = nextLine(prompt);
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese un número entero largo válido.");
            }
        }
    }

    public static BigDecimal nextBigDecimal(String prompt) {
        while (true) {
            String s = nextLine(prompt);
            try {
                return new BigDecimal(s.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Ingrese un número decimal válido (ej: 1234.56).");
            }
        }
    }

    public static BigDecimal nextPositiveMoney(String prompt) {
        while (true) {
            BigDecimal v = nextBigDecimal(prompt);
            if (v.signum() < 0) {
                System.out.println("El valor no puede ser negativo.");
            } else {
                return v;
            }
        }
    }

    public static boolean nextYesNo(String prompt) {
        while (true) {
            String s = nextLine(prompt + " (s/n): ");
            if (s == null) continue;
            s = s.trim().toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sí") || s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Responda con 's' (sí) o 'n' (no).");
        }
    }

    public static boolean nextBoolean(String prompt) {
        return nextYesNo(prompt);
    }

    public static <E extends Enum<E>> E nextEnum(String prompt, Class<E> enumType) {
        String opciones = String.join("/", enumNames(enumType));
        while (true) {
            String s = nextLine(prompt + " [" + opciones + "]: ");
            if (s == null) continue;
            s = s.trim();
            for (E e : enumType.getEnumConstants()) {
                if (e.name().equalsIgnoreCase(s)) return e;
            }
            System.out.println("Valor inválido. Opciones: " + opciones);
        }
    }

    // ===================== Utilidades =====================

    public static void pressEnterToContinue() {
        nextLine("\nPresione ENTER para continuar...");
    }

    private static String[] enumNames(Class<? extends Enum<?>> enumType) {
        Enum<?>[] vals = enumType.getEnumConstants();
        String[] names = new String[vals.length];
        for (int i = 0; i < vals.length; i++) names[i] = vals[i].name();
        return names;
    }
}
