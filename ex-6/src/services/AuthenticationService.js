class AuthenticationService {

    constructor() {
        this.userData = null;
    }

    static setUserData(userData) {
        this.userData = userData;
    }

    static getAuthToken() {
        return this.userData === undefined ? '' : this.userData.token;
    }

    static isAdmin() {
        return this.userData === undefined ? false : this.userData.role === 1;
    }

    static isAuthenticated() {
        return this.userData !== undefined;
    }
}

export default AuthenticationService