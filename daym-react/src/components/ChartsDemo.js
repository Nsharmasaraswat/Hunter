import React, {Component} from 'react';
import {Chart} from 'primereact/chart';

export class ChartsDemo extends Component {

    constructor() {
        super();
        this.state = {
            lineData: {
                labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
                datasets: [
                    {
                        label: 'First Dataset',
                        data: [65, 59, 80, 81, 56, 55, 40],
                        fill: false,
                        borderColor: '#b944d6'
                    },
                    {
                        label: 'Second Dataset',
                        data: [28, 48, 40, 19, 86, 27, 90],
                        fill: false,
                        borderColor: '#0F97C7'
                    }
                ]
            },
            barData: {
                labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
                datasets: [
                    {
                        label: 'My First dataset',
                        backgroundColor: '#0F97C7',
                        borderColor: '#0F97C7',
                        data: [65, 59, 80, 81, 56, 55, 40]
                    },
                    {
                        label: 'My Second dataset',
                        backgroundColor: '#e2841a',
                        borderColor: '#e2841a',
                        data: [28, 48, 40, 19, 86, 27, 90]
                    }
                ]
            },
            pieData: {
                labels: ['A', 'B', 'C'],
                datasets: [
                    {
                        data: [540, 325, 702, 421],
                        backgroundColor: [
                            '#0F97C7',
                            '#b944d6',
                            '#e2841a',
                            '#10b163'
                        ]
                    }]
            },
            polarData: {
                datasets: [{
                    data: [
                        11,
                        16,
                        7,
                        3
                    ],
                    backgroundColor: [
                        '#0F97C7',
                        '#b944d6',
                        '#e2841a',
                        '#10b163'
                    ],
                    label: 'My dataset'
                }],
                labels: [
                    'Blue',
                    'Purple',
                    'Orange',
                    'Green'
                ]
            },
            radarData: {
                labels: ['Eating', 'Drinking', 'Sleeping', 'Designing', 'Coding', 'Cycling', 'Running'],
                datasets: [
                    {
                        label: 'My First dataset',
                        backgroundColor: 'rgba(15,151,199,0.2)',
                        borderColor: 'rgba(15,151,199,1)',
                        pointBackgroundColor: 'rgba(15,151,199,1)',
                        pointBorderColor: '#fff',
                        pointHoverBackgroundColor: '#fff',
                        pointHoverBorderColor: 'rgba(15,151,199,1)',
                        data: [65, 59, 90, 81, 56, 55, 40]
                    },
                    {
                        label: 'My Second dataset',
                        backgroundColor: 'rgba(185,68,214,0.2)',
                        borderColor: 'rgba(185,68,214,1)',
                        pointBackgroundColor: 'rgba(185,68,214,1)',
                        pointBorderColor: '#fff',
                        pointHoverBackgroundColor: '#fff',
                        pointHoverBorderColor: 'rgba(185,68,214,1)',
                        data: [28, 48, 40, 19, 96, 27, 100]
                    }
                ]
            }
        };
    }

    render() {
        return(
            <div className="p-grid p-fluid">
                <div className="p-col-12 p-lg-6">
                    <div className="card">
                        <h1 className="centerText">Linear Chart</h1>
                        <Chart type="line" data={this.state.lineData}/>
                    </div>
        
                    <div className="card">
                        <h1 className="centerText">Pie Chart</h1>
                        <Chart type="pie" data={this.state.pieData} height="150"/>
                    </div>
        
                    <div className="card">
                        <h1 className="centerText">Polar Area Chart</h1>
                        <Chart type="polarArea" data={this.state.polarData} height="150"/>
                    </div>
                </div>
                <div className="p-col-12 p-lg-6">
                    <div className="card">
                        <h1 className="centerText">Bar Chart</h1>
                        <Chart type="bar" data={this.state.barData}/>
                    </div>

                    <div className="card">
                        <h1 className="centerText">Doughnut Chart</h1>
                        <Chart type="doughnut" data={this.state.pieData} height="150"/>
                    </div>

                    <div className="card">
                        <h1 className="centerText">Radar Chart</h1>
                        <Chart type="radar" data={this.state.radarData} height="150"/>
                    </div>
                </div>
            </div>
        )
    }
}
