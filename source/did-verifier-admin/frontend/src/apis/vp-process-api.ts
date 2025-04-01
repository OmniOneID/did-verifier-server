import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const fetchProcesses = async (page: number, size: number, searchKey: string|null, searchValue: string|null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `processes?${params.toString()}`);
};

export const postProcess = async (data: any) => {
    return postData(API_BASE_URL, "processes", data);
}

export const getProcess = async (id: number) => {
    return getData(API_BASE_URL, `processes/${id}`);
}

export const putProcess = async (data: any) => {
    return putData(API_BASE_URL, `processes/${data.id}`, data);
}

export const deleteProcess = async (id: number) => {    
    return deleteData(API_BASE_URL, `processes/${id}`);
}

export const searchProcessList = async (searchValue: string | '') => {    
    return getData(API_BASE_URL, `processes/popups/${searchValue}`);
}