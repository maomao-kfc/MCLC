// 酒店路由

import { RouteRecordRaw } from "vue-router";

const hotelRoutes: RouteRecordRaw = {
  path: "/hotel",
  name: "Hotel",
  component: () => import("@/layout/index.vue"), // 使用后台布局
  meta: {
    title: "酒店管理",
    icon: "Hotel",
    roles: ["admin"],
  },
  children: [
    {
      path: "list",
      name: "HotelList",
      component: () => import("@/views/hotel/index.vue"),
      meta: {
        title: "酒店列表",
        keepAlive: true,
      },
    },
  ],
};

export default hotelRoutes;
