package jp.ac.ritsumei.creditapp.view;

public class CustomData {
        private int hour;
        private String subject;
        private String room;
        private boolean button;
        private int attendNum;

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }

        public int getAttendNum() {
            return attendNum;
        }

        public void setAttendNum(int attendNum) {
            this.attendNum = attendNum;
        }

        public boolean isButton() {
            return button;
        }

        public void setButton(boolean button) {
            this.button = button;
        }
}
