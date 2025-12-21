package app.service;

public class SetParser {
    public static String pickBestSet(String sets) {
        if (sets == null || sets.isBlank()) return "";

        var best = "";
        double bestW = -1;
        int bestR = -1;

        for (String part : sets.split("-")) {
            String[] wr = part.split("x");
            if (wr.length != 2) continue;

            try {
                double w = Double.parseDouble(wr[0]);
                int r = Integer.parseInt(wr[1]);

                if (w > bestW || (Double.compare(w, bestW) == 0 && r > bestR)) {
                    bestW = w;
                    bestR = r;
                    best = wr[0] + "x" + r;
                }
            } catch (NumberFormatException ignore) {
            }
        }
        return best;
    }
}
