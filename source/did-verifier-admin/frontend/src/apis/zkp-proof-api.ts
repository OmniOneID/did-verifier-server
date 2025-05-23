import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const fetchProofRequests = async (page: number, size: number, searchKey: string|null, searchValue: string|null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `zkp/proof-requests?${params.toString()}`);
};

export const deleteProofRequest = async (id: number) => {    
    return deleteData(API_BASE_URL, `zkp/proof-requests/${id}`);
}

export const getCredentialSchemas = async () => {
    return getData(API_BASE_URL, `zkp/proof-requests/credential-schemas`);
}

export const getZkpAttributes = async (namespaceId: number) => {
    return getData(API_BASE_URL, `zkp/namespaces/attributes?namespaceId=${namespaceId}`);
}

export const getCredentialDefinitionsByNamespace = async (namespaceId: number) => {
    return getData(API_BASE_URL, `zkp/namespaces/credential-definitions?namespaceId=${namespaceId}`);
}

export const postProofRequest = async (data: any) => {
    return postData(API_BASE_URL, "zkp/proof-requests", data);
}

export const verifyNameUnique = async (name: string) => {
    return getData(API_BASE_URL, `zkp/proof-requests/check-name?name=${name}`);
}