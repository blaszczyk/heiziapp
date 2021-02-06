package heizi.heizi.data;

import heizi.heizi.data.DataSet;

public class HeiziDataEvaluator {

    public static Message getMessage(final DataSet data) {
        if(data.getPo() <= 60) {
            return new Message("Buffer wird kalt", "Temperatur " + data.getPo() + "°C");
        }
        if(data.getPu() > 80) {
            return new Message("Buffer zu heiss", "Temperatur " + data.getPu() + "°C");
        }
        if(data.getTag() > 300) {
            return new Message("Ofen zu heiss", "Temperatur " + data.getTag() + "°C");
        }
        final long dataAge = System.currentTimeMillis() / 1000L - data.getTime();
        if(dataAge > 300L) {
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
