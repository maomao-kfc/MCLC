/* 会议数据（日程/酒店添加）全局状态 */
import { defineStore } from 'pinia'
import {getMeetingSettingList} from "@/api/portal/meetSetting";


const getSessionGuestList = () => {
    try {
        const data = sessionStorage.getItem('guestList')
        return data ? JSON.parse(data) : []
    } catch {
        return []
    }
}
export const useGuestStore = defineStore('guest', {
    state: () => ({
        guestList: getSessionGuestList(),
        hwList: []
    }),
    actions: {
        async getGuestList(val) {
            try {
                this.guestList = val
                sessionStorage.setItem('guestList', JSON.stringify(val))
            } catch (error) {
                console.error('获取失败', error)
            }
        },
        async gethwList() {
            try {
                const res = await getMeetingSettingList()
                console.log("res getMeetingSettingList", res)
                if(res?.msg === '成功'){
                   this.hwList = res.data
                }else {
                    this.hwList = []
                }
            } catch (error) {
                console.error('获取失败', error)
            }
        }
    }
})
