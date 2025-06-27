import { getData, postData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const getVerifierInfo = async () => {
    return getData(API_BASE_URL, "info");
}
export const registerVerifierInfo = async (data: any) => {
    return postData(API_BASE_URL, `register-verifier-info`, data);
};

export const generateVerifierDidDocument = async () => {
    return postData(API_BASE_URL, `generate-did-auto`, undefined);
}

export const registerVerifierDidDocument = async (data: any) => {
    return postData(API_BASE_URL, `register-did`, data);
};

export const requestEntityStatus = async () => {
    return getData(API_BASE_URL, "request-status");
}

export const requestEnrollEntity = async () => {
    return postData(API_BASE_URL, "request-enroll-entity", undefined);
}