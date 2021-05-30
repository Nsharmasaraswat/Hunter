import axios from 'axios';
import {baseUrl} from "../config/Url";

export class HolidayService {

    editHoliday(id,editData) {
        return axios.patch(baseUrl+'v1/holiday/'+id,editData,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    getAllHolidays() {
        return axios.get(baseUrl+'v1/holiday',{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    createHoliday(name,date,month) {
        return axios.post(baseUrl+'v1/holiday',{
            name,
            date: date,
            month: month
        },{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }

    deleteHoliday(id) {
        return axios.delete(baseUrl+'v1/holiday/'+id,{
            headers: {
                'Authorization': localStorage.getItem('daym-tok')
            }
        }).then(res => res);
    }
}
