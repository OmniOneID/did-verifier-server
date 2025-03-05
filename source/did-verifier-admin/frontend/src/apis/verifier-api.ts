import { getData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const getVerifierInfo = async () => {
    return getData(API_BASE_URL, "verifier/info");
}