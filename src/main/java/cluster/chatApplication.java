package cluster;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public interface chatApplication {

    class userName implements Serializable {
        private String firstName = null;
        private String lastName = null;

        public void setUserName(String name) {
            List<String> fullName = List.of(name.split(" "));
            this.firstName = fullName.get(0);
            if(fullName.size() > 1) this.lastName = fullName.get(1);
        }
        public String getFirstName() { return this.firstName;}
        public String getLastName() { return this.lastName;}
    }

    class messageAll {
        private String message;

        public messageAll(String msg) {
            msg = msg.trim();
            List<String> words = List.of(msg.split(" "));
            this.message = String.join(" ", words.subList(1, words.size()));
        }
        public String getMessage() { return this.message; }
    }

    class messageFew implements Serializable {
        private List<String> userNames;
        private String message;

        public messageFew(String msg) {
            msg = msg.trim();
            List<String> words = List.of(msg.split(" "));
            this.userNames = Arrays.asList(words.get(1).split("\\."));
            this.message = String.join(" ", words.subList(2, words.size()));
        }
        public List<String> getUserNames() { return this.userNames; }
        public String getMessage() { return this.message; }
    }

    class messageTo implements Serializable {
        private String messageTo;
        private String message;

        public messageTo(String msg) {
            msg = msg.trim();
            List<String> words = List.of(msg.split(" "));
            this.messageTo = words.get(0).substring(1);
            this.message = String.join(" ", words.subList(1, words.size()));
        }
        public String getMessageTo() { return this.messageTo; }
        public String getMessage() { return this.message; }
    }

    class messageFrom implements Serializable {
        private String messageFrom;
        private String message;

        public messageFrom(String msgFrom, String msg) {
            this.messageFrom = msgFrom;
            this.setMessage(msg);
        }
        private void setMessage(String msg) { this.message = "\"" + msg + "\""; }
        public String getMessageFrom() { return this.messageFrom; }
        public String getMessage() { return this.message; }
    }

    class onlineUserList {}

    class stopActor {}
}
