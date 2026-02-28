package htl.steyr.uno.server;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class MailSender {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value != null) return value;
        return dotenv.get(key);
    }

    final String username = getEnv("MAIL_USERNAME");
    final String password = getEnv("MAIL_PASSWORD");
    Properties props = new Properties();

    public MailSender() {
        props.put("mail.smtp.host", getEnv("MAIL_HOST"));
        props.put("mail.smtp.port", getEnv("MAIL_PORT"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }

    public void sendAuthenticationCode(String eMail, String authCode) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@uno.clouddb.at", "UNO HTL Steyr"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(eMail));
            message.setSubject("Dein Authentifizierungscode – UNO HTL Steyr");

            MimeMultipart multipart = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Dein Authentifizierungscode: " + authCode + "\n\nGültig für 10 Minuten.\n\nFalls du diese Anfrage nicht gestellt hast, ignoriere diese E-Mail.", "utf-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildHtmlAuthenticationCode(authCode), "text/html; charset=utf-8");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendServerStartetNotification() {
        String eMail = getEnv("MAIL_SUPPORTMAIL");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@uno.clouddb.at", "UNO HTL Steyr"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(eMail));
            message.setSubject("UNO Server gestartet – UNO HTL Steyr");
            message.setText("Der UNO Server wurde erfolgreich gestartet und ist jetzt bereit.", "utf-8");
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildHtmlAuthenticationCode(String authCode) {
        String[] digits = authCode.split("");
        StringBuilder digitBoxes = new StringBuilder();
        for (String digit : digits) {
            digitBoxes.append("""
                <td style="
                    width: 48px; height: 56px;
                    background: #ffffff;
                    border: 2px solid #2e6db4;
                    border-radius: 8px;
                    text-align: center;
                    vertical-align: middle;
                    font-size: 28px;
                    font-weight: 800;
                    color: #1a3a6b;
                    font-family: 'Courier New', monospace;
                    padding: 0 6px;
                ">%s</td>
            """.formatted(digit));
        }

        return """
            <!DOCTYPE html>
            <html lang="de">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f0f4f9;
                         font-family: 'Segoe UI', Arial, sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0"
                     style="background:#f0f4f9; padding: 48px 16px;">
                <tr>
                  <td align="center">
                    <table width="520" cellpadding="0" cellspacing="0"
                           style="background:#ffffff; border-radius:16px; overflow:hidden;
                                  box-shadow: 0 6px 24px rgba(0,0,0,0.10);">

                      <!-- Header -->
                      <tr>
                        <td style="background: linear-gradient(135deg, #1a3a6b 0%%, #2e6db4 100%%);
                                   padding: 40px; text-align: center;">
                          <h1 style="margin:0; color:#ffffff; font-size:22px; font-weight:700;
                                     letter-spacing:1px;">UNO HTL Steyr</h1>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding: 40px 40px 16px;">
                          <p style="margin:0 0 14px; font-size:15px; color:#222;">Hallo,</p>
                          <p style="margin:0 0 32px; font-size:15px; color:#555; line-height:1.7;">
                            wir haben eine Anfrage zur Kontoerstellung mit deiner E-Mail Adresse erhalten.
                            Gib den folgenden <strong>6-stelligen Code</strong> ein,
                            um deine Identität zu bestätigen:
                          </p>

                          <!-- Code Boxes -->
                          <table cellpadding="0" cellspacing="8"
                                 style="margin: 0 auto 12px; border-collapse: separate;">
                            <tr>%s</tr>
                          </table>

                          <p style="text-align:center; margin: 20px 0 0;
                                    font-size:13px; color:#999;">
                            ⏱ Dieser Code ist <strong style="color:#555;">10 Minuten</strong> gültig.
                          </p>
                          <p style="text-align:center; margin: 8px 0 32px;
                                    font-size:13px; color:#bbb;">
                            Du hast diese Anfrage nicht gestellt? Dann ignoriere diese E-Mail.
                          </p>
                        </td>
                      </tr>

                      <!-- Divider -->
                      <tr>
                        <td style="padding: 0 40px;">
                          <hr style="border:none; border-top:1px solid #eee;">
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="padding: 20px 40px 32px; text-align:center;">
                          <p style="margin:0; font-size:12px; color:#ccc;">
                            © 2025 UNO HTL Steyr · Automatisch generierte E-Mail
                          </p>
                          <p style="margin:4px 0 0; font-size:12px; color:#ccc;">
                            Bitte nicht auf diese E-Mail antworten.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.formatted(digitBoxes.toString());
    }


}




