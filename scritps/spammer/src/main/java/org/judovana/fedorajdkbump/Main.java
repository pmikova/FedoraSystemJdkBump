package org.judovana.fedorajdkbump;

    import java.util.Date;
    import javax.mail.*;
    import javax.mail.internet.*;    

    public class Main {

        public static void howToGetOAuthTicket() {
            System.err.println("   Follow: ");
            System.err.println("https://developers.google.com/identity/protocols/oauth2");
            System.err.println("https://support.google.com/cloud/answer/9110914");
            System.err.println("https://support.google.com/googleapi/answer/6158849?hl=en#zippy=");
            System.err.println("https://developers.google.com/identity/protocols/oauth2#2.-obtain-an-access-token-from-the-google-authorization-server.");
            System.err.println("   Use: ");
            System.err.println("https://console.cloud.google.com/apis/credentials/oauthclient/");
            System.err.println("create project, create client for it");
            System.err.println("in client you need client id and client secret forfollowing page, that will guide you");
            System.err.println("https://developers.google.com/oauthplayground");
            System.err.println("will be able to eat client id secret and whatever");
            System.err.println("authorise gmail - https://mail.google.com/");
            System.err.println("   Now it will generate temporary token, whch you can use with your emial id!");
            System.err.println("There are libraries which can do it moreover for you, but afiak you cannto avoid the buyrocracy, so the same verbosity as above wil be reached with libs");
        }

        public static void main(String[] args) throws Exception {
            System.err.println("Current spammer is configured to run with OAuth 2.0 secured gmail");
            System.err.println("If you need to use different email server or different authentification, you have to go with new implementation");
            System.err.println("In addition, it is set to ssl/tls. If you need different, you have to modify this impl");
            System.err.println("WARNING. The OAuth token is temporary! Be sure you send all the emails before it expires (some hour or so)");
            System.err.println(" <email> <oauthToken>  template? list_of_owners? list_of_statuses? ?singleUser?");
            System.err.println(" * template?  the body of email which will be processed and send to every owner of packages");
            System.err.println(" * list_of_owners?  list of all packages we care about, wiht owneres. generated by find-package-maintainers with depndent-packages.jbump as input. Eg maintainers.jbump");
            System.err.println(" * list_of_statuses?  copypasted table from your copr builds, with  buildId/pkg/vr/submitted/duration/status.like.failed|passed");
            System.err.println(" * sinlge_user? Optional param, to spam only one user");
            System.err.println("Trying to spam through  ...");
            if (args.length != 2) {
                System.err.println("Usage: OAuth2Authenticator <email> <oauthToken>  template? list_of_owners? list_of_statuses?");
                System.err.println("eg: jvanek@rh.com ya29.a0...  some/file other/file copypasted/file");
                System.err.println();
                howToGetOAuthTicket();
                return;
            }
            Messagable messagable = com.google.code.samples.oauth2.OAuth2Authenticator.connect(args[0], args[1]);
            Message msg = new MimeMessage(messagable.getSession());
            msg.setFrom(new InternetAddress((args[0])));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse((args[0]), false));
            msg.setSubject("hello from gmial spammer "+System.currentTimeMillis());
            msg.setText("this is body of the fuure mesage for packagers");
            msg.setHeader("X-Mailer", "FedoraSystemJdkBump spamming program");
            msg.setSentDate(new Date());
            messagable.sendMessage(msg);
            messagable.sendMessage(msg);

        }

    }
