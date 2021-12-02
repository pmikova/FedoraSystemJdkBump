package org.judovana.fedorajdkbump;

import org.judovana.fedorajdkbump.builds.BuildsDb;
import org.judovana.fedorajdkbump.people.PeopleDb;
import org.judovana.fedorajdkbump.templates.TemplateLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.stream.Collectors;

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
        System.err.println("will need you to re-login");
        System.err.println("   Now it will generate temporary token, whch you can use with your emial id!");
        System.err.println("There are libraries which can do it moreover for you, but afiak you cannto avoid the buyrocracy, so the same verbosity as above wil be reached with libs");
    }

    public static void main(String[] args) throws Exception {
        System.err.println("Current spammer is configured to run with OAuth 2.0 secured gmail");
        System.err.println("If you need to use different email server or different authentification, you have to go with new implementation");
        System.err.println("In addition, it is set to ssl/tls. If you need different, you have to modify this impl");
        System.err.println("WARNING. The OAuth token is temporary! Be sure you send all the emails before it expires (some hour or so)");
        System.err.println(" <email> <oauthToken>  template? list_of_owners? list_of_statuses?");
        System.err.println(" * template?  the body of email which will be processed and send to every owner of packages");
        System.err.println(" * list_of_owners?  list of all packages we care about, wiht owneres. generated by find-package-maintainers with depndent-packages.jbump as input. Eg maintainers.jbump");
        System.err.println(" * list_of_statuses?  copypasted table from your copr builds, with  buildId/pkg/vr/submitted/duration/status.like.failed|passed");
        System.err.println("Varibale DO have true or single user to test, otherwise jsut test emials are generated");
        System.err.println("Trying to spam through  ...");
        if (args.length != 5) {
            System.err.println("Usage: OAuth2Authenticator <email> <oauthToken>  template? list_of_owners? list_of_statuses?");
            System.err.println("eg: jvanek@rh.com ya29.a0...  some/file other/file copypasted/file");
            System.err.println("To prevent accident spamming, there is variable DO. set it to true,"
                    + " to spam everybody, set it to regex, send emails only to matching persons. Avoid it, to generate all emails to CWD");
            System.err.println();
            howToGetOAuthTicket();
            return;
        }

        Messagable messagable = com.google.code.samples.oauth2.OAuth2Authenticator.connect(args[0], args[1]);
        String DO = System.getenv("DO");

        String templatePath = args[2]; //src/main/resources/maintainer@fedoraproject.org
        PeopleDb people = new PeopleDb(new File(args[3]));//../fillCopr/exemplarResults//maintainers.jbump
        BuildsDb builds = new BuildsDb(new File(args[4]));//./exemplarResults/coprBuildTable.jbump
        if (DO == null || DO.trim().isEmpty()) {
            for (String maintainer : people.getMaintainers()) {
                if (maintainer.matches(".*")) {
                    TemplateLoader email = new TemplateLoader(new File(templatePath), builds, people, maintainer);
                    File f = new File(toFedoraEmail(maintainer));
                    System.out.println("Saving " + f.getAbsolutePath());
                    Files.writeString(f.toPath(), email.getExpandedTemplate());
                }
            }
        } else if (!DO.equalsIgnoreCase("true")) {
            for (String maintainer : people.getMaintainers()) {
                //gmail note. If you are sending email to yourself, anf it is gmail
                //then the message will never be received, will be in you SENT (gmail's sent) "box"
                //that is accessible onl form web  gui. In clients, eg thnderbird, you will not get it (htats why sane user copy every sent message to inbox in thunderbird on gmail)
                if (maintainer.matches(DO)) {
                    TemplateLoader email = new TemplateLoader(new File(templatePath), builds, people, maintainer);
                    System.out.println("Sending to " + toFedoraEmail(maintainer));
                    sendMessage(args[0], toFedoraEmail(maintainer), getSubject(email), email.getExpandedTemplate(), messagable);
                }
            }
        } else {
            for (String maintainer : people.getMaintainers()) {
                if (antispam(maintainer)) {
                    TemplateLoader email = new TemplateLoader(new File(templatePath), builds, people, maintainer);
                    System.out.println("Sending to " + toFedoraEmail(maintainer));
                    sendMessage(args[0], toFedoraEmail(maintainer), getSubject(email), email.getExpandedTemplate(), messagable);
                    Thread.sleep(1000);
                } else {
                    System.out.println("Skipping: " + toFedoraEmail(maintainer) + " already spammed according to curent antispam list");
                }
            }
        }

    }

    private static String antispam = null;

    private static boolean antispam(String maintainer) {
        //Even with sleep, gmail can kick you off. IN that case, populate resources/antispam
        //with - from terminal copypasted:
        //...gmail.com  OAUTH 2.0!
        //.based on https://github.com/google/gmail-oauth2-tools/blob/downloads/oauth2-java-sample-20120904.zip
        //Successfully authenticated to SMTP.
        //Sending to ebaron@fedoraproject.org
        //Sent to: ebaron@fedoraproject.org
        //Sending to lorenzodalrio@fedoraproject.org
        //...
        //Sent to: oget@fedoraproject.org
        //Sending to lupinix@fedoraproject.org
        //Exception in thread "main" com.sun.mail.smtp.SMTPSendFailedException: 421 4.7.0 Try again later, closing connection. (MAIL) sb8sm1568893ejc.51 - gsmtp
        //
        //The maintainers antispam file is then checked here, and if hman was already notifirf, they are not spammed again
        if (antispam == null) {
            antispam = "startword " + new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/antispam"), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining(" ")) + " stopword";
        }
        return (!antispam.contains(" " + toFedoraEmail(maintainer) + " "));
    }

    private static String getSubject(TemplateLoader tl) {
        return tl.getMacro("<AFFECTED_FC>") + " mass rebuild for " + tl.getMacro("<JDK_TO>") + " in copr I. Your personal summary.";
    }

    private static String toFedoraEmail(String maintainer) {
        return maintainer + "@fedoraproject.org";
    }

    private static void sendMessage(String from, String to, String subject, String body, Messagable messagable) throws MessagingException {
        Message msg = new MimeMessage(messagable.getSession());
        msg.setFrom(new InternetAddress((from)));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse((to), false));
        msg.setSubject(subject);
        msg.setText(body);
        msg.setHeader("X-Mailer", "FedoraSystemJdkBump spamming program");
        msg.setSentDate(new Date());
        messagable.sendMessage(msg);
    }

}
