package net.engineerAnsh.journalApp.enums;

public enum GoogleProvisioningStatus {

    /**
     * Existing Google account or existing LOCAL account
     * successfully linked during LOGIN.
     */
    AUTHENTICATE,

    /**
     * LOGIN was attempted but no JournalFlow account
     * exists yet.
     */
    SIGNUP_REQUIRED,

    /**
     * SIGNUP successfully created a new Google account.
     */
    ACCOUNT_CREATED,

    /**
     * SIGNUP was attempted but an account already exists.
     */
    ACCOUNT_EXISTS
}