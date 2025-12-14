import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

class ScientificCalculator extends JFrame {

    // --- Colors ---
    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color DISPLAY_BG = new Color(220, 230, 235);
    private static final Color BTN_DARK = new Color(50, 55, 60);
    private static final Color BTN_NUM = new Color(60, 65, 70);
    private static final Color BTN_ORANGE = new Color(230, 126, 34);
    private static final Color BTN_BLUE = new Color(41, 128, 185);
    private static final Color BTN_YELLOW = new Color(241, 196, 15);
    private static final Color BTN_PURPLE = new Color(155, 89, 182);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_BLACK = Color.BLACK;

    private JTextField displayField;
    private String expression = "";

    public ScientificCalculator() {
        setTitle("Scientific Calculator");
        setSize(400, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // --- Display ---
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBackground(BG_COLOR);
        displayPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        displayField = new JTextField();
        displayField.setFont(new Font("Segoe UI", Font.BOLD, 32));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(false);
        displayField.setBackground(DISPLAY_BG);
        displayField.setForeground(Color.BLACK);
        displayField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        displayPanel.add(displayField, BorderLayout.CENTER);
        add(displayPanel, BorderLayout.NORTH);

        // --- Keypad ---
        JPanel keypadPanel = new JPanel();
        keypadPanel.setLayout(new GridLayout(7, 5, 8, 8));
        keypadPanel.setBackground(BG_COLOR);
        keypadPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        // Updated Buttons Array: Replaced top row with direct Math functions
        String[] buttons = {
                "π",    "e",    "|x|",  "n!",   "mod",   // Row 1: Constants & Special Ops
                "x²",   "x³",   "sqrt", "^",    "log",
                "sin",  "cos",  "tan",  "(",    ")",
                "7",    "8",    "9",    "DEL",  "AC",
                "4",    "5",    "6",    "*",    "/",
                "1",    "2",    "3",    "+",    "-",
                "0",    ".",    "EXP",  "Ans",  "="
        };

        for (String text : buttons) {
            Color btnColor = BTN_DARK;
            Color fgColor = TEXT_WHITE;

            // Color Logic
            if (text.matches("[0-9]") || text.equals(".")) {
                btnColor = BTN_NUM;
            } else if (text.equals("AC") || text.equals("DEL")) {
                btnColor = BTN_ORANGE;
                fgColor = TEXT_BLACK;
            } else if (text.equals("=")) {
                btnColor = BTN_BLUE; // Highlight Equals
            } else if (text.equals("π")) {
                btnColor = BTN_YELLOW;
                fgColor = TEXT_BLACK;
            } else if (text.equals("e")) {
                btnColor = BTN_PURPLE;
            }

            RoundedButton btn = new RoundedButton(text, btnColor, fgColor);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.addActionListener(new ButtonClickListener());
            keypadPanel.add(btn);
        }

        add(keypadPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            switch (command) {
                case "AC":
                    expression = "";
                    break;
                case "DEL":
                    if (!expression.isEmpty()) {
                        expression = expression.substring(0, expression.length() - 1);
                    }
                    break;
                case "=":
                    try {
                        double result = evaluateExpression(expression);
                        // integer check
                        if(result == (long) result) {
                            expression = String.format("%d", (long)result);
                        } else {
                            expression = String.valueOf(result);
                        }
                    } catch (Exception ex) {
                        expression = "Error";
                    }
                    break;
                case "x²": expression += "^2"; break;
                case "x³": expression += "^3"; break;
                case "sqrt": expression += "sqrt("; break;
                case "log": expression += "log("; break;
                case "ln":  expression += "ln("; break;
                case "sin":
                case "cos":
                case "tan": expression += command + "("; break;
                case "|x|": expression += "abs("; break;
                case "mod": expression += "%"; break;
                case "n!":  expression += "!"; break;
                case "π":   expression += "pi"; break;
                case "e":   expression += "e"; break;
                case "EXP": expression += "E"; break;
                case "Ans": break; // Placeholder
                default:    expression += command;
            }
            displayField.setText(expression);
        }
    }

    // --- Enhanced Math Parser ---
    private double evaluateExpression(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else if (eat('%')) x %= parseFactor(); // Added Modulo support
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') {
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = expr.substring(startPos, this.pos);

                    if (func.equals("sqrt")) x = Math.sqrt(parseFactor());
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(parseFactor()));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(parseFactor()));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(parseFactor()));
                    else if (func.equals("log")) x = Math.log10(parseFactor());
                    else if (func.equals("abs")) x = Math.abs(parseFactor());
                    else if (func.equals("pi")) x = Math.PI;
                    else if (func.equals("e"))  x = Math.E;
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                // Handle Factorial (post-fix operator)
                if (eat('!')) {
                    x = factorial(x);
                }

                // Handle Power
                if (eat('^')) x = Math.pow(x, parseFactor());

                return x;
            }

            // Helper for Factorial
            double factorial(double n) {
                if (n < 0) return 0; // Error case
                if (n == 0) return 1;
                double res = 1;
                for (int i = 1; i <= n; i++) res *= i;
                return res;
            }
        }.parse();
    }

    // --- Custom Rounded Button ---
    class RoundedButton extends JButton {
        private Color bgColor, fgColor;

        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fgColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScientificCalculator().setVisible(true));
    }
}