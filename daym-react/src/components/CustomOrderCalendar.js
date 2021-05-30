import React, {useRef, useState, useEffect} from 'react';
import moment from "moment";
import {Growl} from "primereact/growl";
import './Table.css';
import {Button} from "primereact/button";
import {useTranslation} from "react-i18next";
// import { Tooltip } from 'primereact/tooltip';

let skip = 0;

export default function CustomOrderCalendar({orders}) {
    const growl = useRef(null);
    const {t} = useTranslation();
    const [allTimes, setAllTimes] = useState([]);
    const [allOrders, setAllOrders] = useState([]);
    const [allDocs, setAllDocs] = useState([]);
    const [currentDate, setCurrentDate] = useState(moment(new Date()).format('DD/MM/YYYY'));

    useEffect(() => {
        if(orders.length > 0){
            const uniqueOrders = orders.filter((item, index, array) => {
                return array.findIndex(t => t['dock']['_id'] === item['dock']['_id']) === index;
            });
            const _docks = uniqueOrders.map(each => each.dock);
            setAllOrders(orders.map((e,i) => {
                let _e = e;
                _e.deliveryTime = moment(_e.deliveryTime).format('hh:mm A');
                _e.deliveryOn = moment(_e.deliveryOn).format('DD/MM/YYYY');
                // console.log(i+'--->'+_e.deliveryOn+'--'+_e.deliveryTime+'-->'+_e.dock.name)
                return _e;
            }));
            setAllDocs(_docks);

            const minStart = _docks.map(e => {
                let _e = e;
                _e.startingTime = moment(_e.startingTime).valueOf();
                return _e;
            }).sort((a, b) =>
                b.startingTime - a.startingTime
            )[0].startingTime;

            const maxEnd = _docks.map(e => {
                let _e = e;
                // console.log('Dock ',e.name+' --- ', moment(e.endingTime).format('hh:mm A'));
                _e.endingTime = moment(_e.endingTime).valueOf();
                return _e;
            }).sort((a, b) =>
                b.endingTime - a.endingTime
            )[_docks.length - 1].endingTime;
            let t = minStart;
            let _times = [];
            for (; moment(t).format('hh:mm A') !== moment(maxEnd).format('hh:mm A');) {
                _times.push(moment(t).format('hh:mm A'));
                t = moment(t).add(15, 'm');
            }
            _times.push(moment(maxEnd).format('hh:mm A'));
            setAllTimes(_times);
        }
    }, [orders]);

    if(orders.length === 0)
        return <div style={{
            width: '90vw',
            height: '10vh',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center'
        }}>
            <p>{t('no_orders')}</p>
        </div>

    return <div style={{width: '90vw', overflow: 'scroll', margin: '10px 0px'}}>
        <Growl ref={growl} style={{marginTop: '75px'}}/>
        <div style={{maxWidth: '90vw', overflow: 'scroll'}}>
            <div style={{display: 'flex', margin: '8px 0px'}}>
                <Button icon="pi pi-angle-left" onClick={() => {
                    let _current = currentDate;
                    setCurrentDate(moment(_current, 'DD/MM/YYYY').subtract(1, 'd').format('DD/MM/YYYY'));
                }}/>
                <p style={{minWidth: 180, marginTop: 'auto', marginBottom: 'auto', textAlign: 'center'}}>
                    {currentDate}
                </p>
                <Button icon="pi pi-angle-right" onClick={() => {
                    let _current = currentDate;
                    setCurrentDate(moment(_current, 'DD/MM/YYYY').add(1, 'd').format('DD/MM/YYYY'));
                }}/>

                <p style={{
                    minWidth: 120,
                    margin: '0px 20px',
                    paddingTop: '7px',
                    color: 'white',
                    textAlign: 'center',
                    background: 'orange'
                }}>
                    Pending
                </p>
                <p style={{
                    minWidth: 120,
                    margin: '0px 20px',
                    paddingTop: '7px',
                    color: 'white',
                    textAlign: 'center',
                    background: 'green'
                }}>
                    Complete
                </p>

            </div>
            <div style={{border: '1px solid #323232', width: 'fit-content'}}>
                <div style={{display: 'flex'}}>
                    <div className="tdC">Time Slots</div>
                    {
                        allDocs.map((each) =>
                            <div className="tdC">{`${each.name}(${each.parentId.name})`}</div>
                        )
                    }
                </div>
                <div style={{display: 'flex'}}>
                    <div className="tableC">
                        {
                            allTimes.map((each) =>
                                <div className="tdC">{each}</div>
                            )
                        }
                    </div>
                    {
                        allDocs.map((dock)=>
                            <div style={{display: 'flex', flexDirection: 'column'}}>
                                {
                                    allTimes.map((each) => {
                                        let _order = allOrders.filter((e) =>
                                            e.deliveryTime === each && dock.name === e.dock.name && dock.parentId._id === e.dock.parentId._id && currentDate === e.deliveryOn
                                        );
                                        if(skip > 0){
                                            skip--;
                                            return <></>;
                                        }else{
                                            if(_order.length > 0){
                                                const length = _order[0].slots.length;
                                                skip = _order[0].slots.length - 1;
                                                return <div
                                                    className="thC"
                                                    // tooltip={
                                                    //     `${_order[0].product.name}(${_order[0].appointment.supplier.name})`
                                                    // }
                                                    style={{
                                                        height: `${length * 30}px`,
                                                        cursor: 'pointer',
                                                        wordBreak: 'break-all',
                                                        background: _order[0].status === 1 ? 'orange' : 'green',
                                                    }} onClick={()=>{
                                                        window.location = "#/manage-appointment/"+_order[0].appointment._id;
                                                    }}>
                                                        {
                                                            `${_order[0].product.name}(${_order[0].appointment.supplier.name})`
                                                        }
                                                    </div>;
                                            }else
                                                return <div className="thC"/>;
                                        }
                                    })
                                }
                            </div>
                        )
                    }
                </div>
            </div>
        </div>
    </div>

}

















































































































































