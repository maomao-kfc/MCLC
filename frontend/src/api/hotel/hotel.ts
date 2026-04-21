//酒店管理API

import request from "@/utils/request";
import type { Hotel, PageResult } from "@/views/hotel/types";

// 酒店分页列表
// export function getHotelPage(params: any) {
//   return request({
//     url: "/api/v1/hotel/page",
//     method: "get",
//     params,
//   });
// }

export function getHotelPage(params: any): Promise<PageResult<Hotel>> {
  return request.get("/api/v1/hotel/page", { params });
}

// 新增酒店
export function addHotel(data: any) {
  return request({
    url: "/api/v1/hotel",
    method: "post",
    data,
  });
}

// 修改酒店
export function updateHotel(data: any) {
  return request({
    url: `/api/v1/hotel/${data.id}`,
    method: "put",
    data,
  });
}

// 删除酒店
export function deleteHotel(id: number) {
  return request({
    url: `/api/v1/hotel/${id}`,
    method: "delete",
  });
}

// 获取酒店详情
export function getHotelDetail(id: number) {
  return request({
    url: `/api/v1/hotel/${id}`,
    method: "get",
  });
}
