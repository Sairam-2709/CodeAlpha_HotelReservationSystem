package util;

/**
 * Very simple payment simulator: validates basic fields and returns success/failure.
 * This is not real payment processing.
 */
public class PaymentSimulator {

    public static class PaymentResult {
        public final boolean success;
        public final String message;
        public PaymentResult(boolean success, String message) { this.success = success; this.message = message; }
    }

    /**
     * Simulate payment: accept if cardNumber length 12-19 digits, cvv 3-4 digits, non-empty name.
     */
    public static PaymentResult process(String cardHolder, String cardNumber, String expiry, String cvv, double amount) {
        if (cardHolder == null || cardHolder.trim().isEmpty()) return new PaymentResult(false, "Card holder required");
        if (cardNumber == null || !cardNumber.matches("\\d{12,19}")) return new PaymentResult(false, "Invalid card number");
        if (cvv == null || !cvv.matches("\\d{3,4}")) return new PaymentResult(false, "Invalid CVV");
        if (expiry == null || !expiry.matches("(0[1-9]|1[0-2])/(\\d{2})")) return new PaymentResult(false, "Expiry must be MM/YY");

        // Very naive expiry check: accept everything — we could parse more but not necessary for simulation
        // Randomly fail 5% for realism
        if (Math.random() < 0.05) return new PaymentResult(false, "Payment gateway error (simulated). Try again.");

        return new PaymentResult(true, "Payment successful (simulated). Amount charged: ₹" + amount);
    }
}
