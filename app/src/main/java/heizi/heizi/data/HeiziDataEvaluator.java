package heizi.heizi.data;

public class HeiziDataEvaluator {

    public static Message getMessage(final DataSet data) {
        final long currentTimeSec = System.currentTimeMillis() / 1000L;
        final long turAge = currentTimeSec - data.getTur();
        if(data.getPo() < 60 && data.getTag() < 150 && turAge > 300) {
            final String tempus = data.getPo() < 45 ? "ist" : "wird";
            return new Message("Speicher " + tempus + " kalt", "Temperatur " + data.getPo() + "°C");
        }
        if(data.getPu() > 80) {
            return new Message("Speicher zu heiss", "Temperatur " + data.getPu() + "°C");
        }
        if(data.getTag() > 300) {
            return new Message("Ofen zu heiss", "Temperatur " + data.getTag() + "°C");
        }
        final long dataAge = currentTimeSec - data.getTime();
        if(dataAge > 300L && turAge > 300L) {
            return new Message("Keine neuen Daten", "seit " + dataAge + " sec.");
        }
        return null;
    }

    public static class Message {
        private final String title;
        private final String text;

        public Message(String title, String text) {
            this.title = title;
            this.text = text;
        }

        public String getTitle() {
            return title;
        }

        public String getText() {
            return text;
        }
    }
}
