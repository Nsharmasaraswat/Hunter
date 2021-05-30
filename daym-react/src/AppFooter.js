import React, { Component } from 'react';

export class AppFooter extends Component {

    render() {
        return <div className="layout-footer">
            <div className="p-grid">
                <div className="p-col-6">
                    <h2>HUNTER</h2>
                    <p>Powered By GTP Automation</p>
                </div>
                <div className="p-col-6 footer-icons">
                    <button className="p-link">
                        <i className="pi pi-home"></i>
                    </button>
                    <button className="p-link">
                        <i className="pi pi-globe"></i>
                    </button>
                    <button className="p-link">
                        <i className="pi pi-envelope"></i>
                    </button>
                </div>
            </div>
        </div>
    }
}
