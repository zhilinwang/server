package org.osiam.web.util

import org.osiam.resources.scim.MultiValuedAttribute
import org.osiam.resources.scim.User
import org.spockframework.util.Assert
import spock.lang.Specification

import javax.mail.Message
import javax.mail.internet.MimeMessage
import javax.servlet.ServletContext

/**
 * Created with IntelliJ IDEA.
 * User: Igor
 * Date: 19.11.13
 * Time: 09:13
 * To change this template use File | Settings | File Templates.
 */
class MailSenderTest extends Specification {

    def "Sends a test-mail"() {
        given:
        def mailSender = Spy(MailSender)
        def content = new ByteArrayInputStream("please please please! \$FIRSTNAME \$LASTNAME".bytes)

        def contentVars = ["\$FIRSTNAME": "donald", "\$LASTNAME": "duck"]

        when:
        mailSender.sendMail("donald.duck@example.org", "uncle.scroogle@example.org", "need money", content, contentVars);

        then:
        1 * mailSender.transportMail(_) >> { MimeMessage message ->
            Assert.that(message.getFrom()[0].toString().equals("donald.duck@example.org"), "from dont match!" )
            Assert.that(message.getRecipients(Message.RecipientType.TO)[0].toString().equals("uncle.scroogle@example.org"), "to dont match!" )
            Assert.that(message.getSubject().equals("need money"), "subject dont match!")
            Assert.that("please please please! donald duck".equals(message.getContent()), "content dont match")
        }

    }

    def "getting primary email from user"() {
        given:
        def mailSender = new MailSender()
        def thePrimaryMail = "primary@mail.com"

        def theEmail = new MultiValuedAttribute.Builder().setPrimary(true).setValue(thePrimaryMail).build()
        def user = new User.Builder("theMan").setEmails([theEmail] as List).build()

        when:
        def email = mailSender.extractPrimaryEmail(user)

        then:
        email == thePrimaryMail
    }

    def "should return null if no primary email was found"() {
        given:
        def mailSender = new MailSender()
        def thePrimaryMail = "primary@mail.com"

        def theEmail = new MultiValuedAttribute.Builder().setPrimary(false).setValue(thePrimaryMail).build()
        def user = new User.Builder("theMan").setEmails([theEmail] as List).build()

        when:
        def email = mailSender.extractPrimaryEmail(user)

        then:
        email == null
    }

    def "should not throw exception if users emails are not present"() {
        given:
        def mailSender = new MailSender()

        def user = new User.Builder("theMan").build()

        when:
        def email = mailSender.extractPrimaryEmail(user)

        then:
        email == null
    }

    def "should read the email content from default path if user defined path is null"() {
        given:
        def mailSender = new MailSender()
        def contextMock = Mock(ServletContext)

        def inputStream = new ByteArrayInputStream("the email content".bytes)

        when:
        def result = mailSender.getEmailContentAsStream("defaultPath", null, contextMock)

        then:
        1 * contextMock.getResourceAsStream("defaultPath") >> inputStream
        result == inputStream
    }

    def "should read the email content from default path if user defined path is empty"() {
        given:
        def mailSender = new MailSender()
        def contextMock = Mock(ServletContext)

        def inputStream = new ByteArrayInputStream("the email content".bytes)

        when:
        def result = mailSender.getEmailContentAsStream("defaultPath", "", contextMock)

        then:
        1 * contextMock.getResourceAsStream("defaultPath") >> inputStream
        result == inputStream
    }

    def "should read the email content from user defined path if it is not null"() {
        given:
        def mailSender = new MailSender()
        def contextMock = Mock(ServletContext)

        def url = this.getClass().getResource("/test-content.txt")

        when:
        def result = mailSender.getEmailContentAsStream("defaultPath", url.getFile(), contextMock)

        then:
        def fileAsString = getStringFromStream(result)
        fileAsString == "Just a test!"
    }

    def getStringFromStream(result) {
        def builder = new StringBuilder()
        int ch
        while((ch = result.read()) != -1){
            builder.append((char)ch)
        }

        return builder.toString()
    }
}