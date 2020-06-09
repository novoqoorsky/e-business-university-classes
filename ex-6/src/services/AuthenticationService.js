import React from 'react';

class AuthenticationService {

    constructor() {}

    static setUserData(userData) {
        this.userData = userData;
    }

    static getAuthToken() {
        return this.userData === undefined ? '' : this.userData.token;
    }

    static isAdmin() {
        return this.userData === undefined ? false : this.userData.role === "Admin";
    }

    static isAuthenticated() {
        return this.userData !== undefined;
    }
}

export default AuthenticationService