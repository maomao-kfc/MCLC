import request from "@/utils/request";
import type { FileInfo } from "@/types/api";

const FileAPI = {
  /** 上传文件 （传入 FormData，上传进度回调） */
  upload(formData: FormData, onProgress?: (percent: number) => void) {
    return request<any, FileInfo>({
      url: "/api/v1/files",
      method: "post",
      data: formData,
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 3600000, // 1小时，与nginx/backend超时对齐，支持大文件上传
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress?.(percent);
        }
      },
    });
  },

  /** 上传文件（传入 File） */
  uploadFile(file: File) {
    const formData = new FormData();
    formData.append("file1", file);
    return request<any, any>({
      url: "/api/v1/files",
      method: "post",
      data: formData,
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 3600000, // 1小时，与nginx/backend超时对齐，支持大文件上传
    });
  },

  /** 删除文件 */
  delete(filePath?: string) {
    return request({
      url: "/api/v1/files",
      method: "delete",
      params: { filePath },
    });
  },

  /** 下载文件 */
  download(url: string, fileName?: string) {
    return request({
      url,
      method: "get",
      responseType: "blob",
    }).then((res) => {
      const blob = new Blob([res.data]);
      const a = document.createElement("a");
      const urlObject = window.URL.createObjectURL(blob);
      a.href = urlObject;
      a.download = fileName || "下载文件";
      a.click();
      window.URL.revokeObjectURL(urlObject);
    });
  },
};

export default FileAPI;
