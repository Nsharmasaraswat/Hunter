export interface Report {
    name: string;
    file: string;
    query: string;
    variables: ReportVariable[];
    columns: ReportColumn[];
    actions: Action[]
}

export interface ReportColumn {
    field: string;
    header: string;
    type: string;
    nullString: string;
    priority?: number;
    width?: string;
    min_width?: string;
}

export interface ReportVariable {
    field: string;
    var: string;
    type: string;
    options?: ReportVariableOption[];
}

export interface ReportVariableOption {
    label: string;
    value: string;
}

export interface Action {
    name: string;
    icon: string;
    action: string;
    field: string;
}