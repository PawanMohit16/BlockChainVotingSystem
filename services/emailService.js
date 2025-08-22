const nodemailer = require('nodemailer');

class EmailService {
  constructor() {
    // Create transporter for sending emails
    this.transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST || 'smtp.gmail.com',
      port: process.env.SMTP_PORT || 587,
      secure: false, // true for 465, false for other ports
      auth: {
        user: process.env.SMTP_USER || 'your-email@gmail.com',
        pass: process.env.SMTP_PASS || 'your-app-password'
      }
    });
  }

  // Send OTP email
  async sendOTP(email, otp) {
    try {
      const mailOptions = {
        from: process.env.SMTP_USER || 'your-email@gmail.com',
        to: email,
        subject: 'Voting System - Email Verification OTP',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #333; text-align: center;">Voting System Verification</h2>
            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
              <h3 style="color: #007bff; margin-bottom: 15px;">Your Verification Code</h3>
              <div style="background-color: #007bff; color: white; font-size: 24px; font-weight: bold; text-align: center; padding: 15px; border-radius: 5px; letter-spacing: 5px;">
                ${otp}
              </div>
              <p style="margin-top: 15px; color: #666;">
                This code will expire in 10 minutes. Please enter it on the verification page to complete your registration.
              </p>
            </div>
            <div style="text-align: center; color: #666; font-size: 14px;">
              <p>If you didn't request this code, please ignore this email.</p>
              <p>This is an automated message, please do not reply.</p>
            </div>
          </div>
        `
      };

      const result = await this.transporter.sendMail(mailOptions);
      console.log('OTP email sent successfully:', result.messageId);
      return { success: true, messageId: result.messageId };
    } catch (error) {
      console.error('Failed to send OTP email:', error);
      throw new Error('Failed to send OTP email');
    }
  }

  // Send password reset email
  async sendPasswordReset(email, resetToken) {
    try {
      const resetUrl = `${process.env.FRONTEND_URL || 'http://localhost:3000'}/reset-password?token=${resetToken}`;
      
      const mailOptions = {
        from: process.env.SMTP_USER || 'your-email@gmail.com',
        to: email,
        subject: 'Voting System - Password Reset Request',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #333; text-align: center;">Password Reset Request</h2>
            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
              <p style="color: #666; margin-bottom: 15px;">
                You requested a password reset for your voting system account. Click the button below to reset your password:
              </p>
              <div style="text-align: center;">
                <a href="${resetUrl}" style="background-color: #dc3545; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                  Reset Password
                </a>
              </div>
              <p style="margin-top: 15px; color: #666; font-size: 14px;">
                If the button doesn't work, copy and paste this link into your browser:
              </p>
              <p style="word-break: break-all; color: #007bff; font-size: 12px;">
                ${resetUrl}
              </p>
            </div>
            <div style="text-align: center; color: #666; font-size: 14px;">
              <p>This link will expire in 1 hour.</p>
              <p>If you didn't request a password reset, please ignore this email.</p>
            </div>
          </div>
        `
      };

      const result = await this.transporter.sendMail(mailOptions);
      console.log('Password reset email sent successfully:', result.messageId);
      return { success: true, messageId: result.messageId };
    } catch (error) {
      console.error('Failed to send password reset email:', error);
      throw new Error('Failed to send password reset email');
    }
  }

  // Send vote confirmation email
  async sendVoteConfirmation(email, electionTitle) {
    try {
      const mailOptions = {
        from: process.env.SMTP_USER || 'your-email@gmail.com',
        to: email,
        subject: 'Voting System - Vote Confirmation',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #333; text-align: center;">Vote Confirmation</h2>
            <div style="background-color: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
              <h3 style="color: #155724; margin-bottom: 15px;">✓ Your Vote Has Been Recorded</h3>
              <p style="color: #155724; margin-bottom: 10px;">
                <strong>Election:</strong> ${electionTitle}
              </p>
              <p style="color: #155724; margin-bottom: 10px;">
                <strong>Date:</strong> ${new Date().toLocaleDateString()}
              </p>
              <p style="color: #155724; margin-bottom: 10px;">
                <strong>Time:</strong> ${new Date().toLocaleTimeString()}
              </p>
            </div>
            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
              <h4 style="color: #333; margin-bottom: 15px;">Important Information:</h4>
              <ul style="color: #666; padding-left: 20px;">
                <li>Your vote has been securely recorded using blockchain technology</li>
                <li>You cannot vote again in this election</li>
                <li>Results will be available after the election ends</li>
                <li>Thank you for participating in the democratic process!</li>
              </ul>
            </div>
            <div style="text-align: center; color: #666; font-size: 14px;">
              <p>This is an automated confirmation message.</p>
            </div>
          </div>
        `
      };

      const result = await this.transporter.sendMail(mailOptions);
      console.log('Vote confirmation email sent successfully:', result.messageId);
      return { success: true, messageId: result.messageId };
    } catch (error) {
      console.error('Failed to send vote confirmation email:', error);
      throw new Error('Failed to send vote confirmation email');
    }
  }

  // Send account approval email
  async sendApprovalEmail(email, firstName) {
    try {
      const mailOptions = {
        from: process.env.SMTP_USER || 'your-email@gmail.com',
        to: email,
        subject: 'Voting System - Account Approved',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #333; text-align: center;">Account Approval</h2>
            <div style="background-color: #d4edda; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
              <h3 style="color: #155724; margin-bottom: 15px;">✓ Your Account Has Been Approved!</h3>
              <p style="color: #155724; margin-bottom: 10px;">
                Dear <strong>${firstName}</strong>,
              </p>
              <p style="color: #155724; margin-bottom: 10px;">
                Congratulations! Your voting system account has been approved by our administrators. You can now:
              </p>
              <ul style="color: #155724; padding-left: 20px;">
                <li>Log in to your account</li>
                <li>Participate in active elections</li>
                <li>Cast your votes securely</li>
              </ul>
            </div>
            <div style="text-align: center; margin: 20px 0;">
              <a href="${process.env.FRONTEND_URL || 'http://localhost:3000'}/login" style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                Login Now
              </a>
            </div>
            <div style="text-align: center; color: #666; font-size: 14px;">
              <p>Thank you for your patience during the approval process.</p>
            </div>
          </div>
        `
      };

      const result = await this.transporter.sendMail(mailOptions);
      console.log('Approval email sent successfully:', result.messageId);
      return { success: true, messageId: result.messageId };
    } catch (error) {
      console.error('Failed to send approval email:', error);
      throw new Error('Failed to send approval email');
    }
  }

  // Send account rejection email
  async sendRejectionEmail(email, firstName, reason) {
    try {
      const mailOptions = {
        from: process.env.SMTP_USER || 'your-email@gmail.com',
        to: email,
        subject: 'Voting System - Account Application Status',
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
            <h2 style="color: #333; text-align: center;">Account Application Status</h2>
            <div style="background-color: #f8d7da; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
              <h3 style="color: #721c24; margin-bottom: 15px;">Account Application Not Approved</h3>
              <p style="color: #721c24; margin-bottom: 10px;">
                Dear <strong>${firstName}</strong>,
              </p>
              <p style="color: #721c24; margin-bottom: 10px;">
                We regret to inform you that your voting system account application has not been approved at this time.
              </p>
              <div style="background-color: #f1f3f4; padding: 15px; border-radius: 5px; margin: 15px 0;">
                <strong>Reason:</strong> ${reason}
              </div>
              <p style="color: #721c24; margin-bottom: 10px;">
                If you believe this decision was made in error or if you would like to provide additional information, please contact our support team.
              </p>
            </div>
            <div style="text-align: center; color: #666; font-size: 14px;">
              <p>We appreciate your interest in our voting system.</p>
            </div>
          </div>
        `
      };

      const result = await this.transporter.sendMail(mailOptions);
      console.log('Rejection email sent successfully:', result.messageId);
      return { success: true, messageId: result.messageId };
    } catch (error) {
      console.error('Failed to send rejection email:', error);
      throw new Error('Failed to send rejection email');
    }
  }

  // Test email service
  async testConnection() {
    try {
      await this.transporter.verify();
      console.log('Email service connection verified successfully');
      return true;
    } catch (error) {
      console.error('Email service connection failed:', error);
      return false;
    }
  }
}

module.exports = new EmailService();
