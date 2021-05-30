
export var options = {
  actions: [
    {
      name: "grouping",
      display: "Agrupar",
      actions: [
        {
          name: "group",
          display: "Ocorrência",
          fieldType: ["any"],
        },
        {
          name: "count",
          display: "Contar",
          fieldType: ["any"],
        },
        {
          name: "group-year",
          display: "Por ano",
          fieldType: ["java.sql.Timestamp", "java.sql.Date"],
        },
        {
          name: "group-month",
          display: "Por mês",
          fieldType: ["java.sql.Timestamp", "java.sql.Date"],
        },
        {
          name: "group-month",
          display: "Por dia",
          fieldType: ["java.sql.Timestamp", "java.sql.Date"],
        },
        {
          name: "group-hour",
          display: "Por hora",
          fieldType: ["java.sql.Timestamp"],
        },
      ],
    },
    {
      name: "summing",
      display: "Somar",
      actions: [
        {
          name: "sum",
          display: "Somar",
          fieldType: [
            "java.lang.Integer",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Number",
          ],
        },
      ],
    },
    {
      name: "averaging",
      display: "Média",
      actions: [
        {
          name: "average",
          display: "Média",
          fieldType: [
            "java.lang.Integer",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Number",
          ],
        },
      ],
    },
  ],
  lengendPosition: [
    { name: "right", display: "Direita" },
    { name: "below", display: "Em Baixo" },
  ],
  widgetType: [
    { name: "card", display: "Cartão" },
    { name: "chart", display: "Gráfico" },
    { name: "table", display: "Tabela" },
  ],
  chartTypes: [
    {
      name: "lines",
      display: "Linhas e Área",
      chartTypes: [
        { name: "line", display: "Padrão", dataType: "multi" },
        { name: "area", display: "Área", dataType: "multi" },
        { name: "polar", display: "Área Polar", dataType: "multi" },
        { name: "area-stacked", display: "Área Empilhado", dataType: "multi" },
        {
          name: "area-normalized",
          display: "Area normalizada",
          dataType: "multi",
        },
      ],
    },
    {
      name: "bars-vertical",
      display: "Barras Verticais",
      chartTypes: [
        { name: "bar-vertical", display: "Padrão", dataType: "single" },
        {
          name: "bar-vertical-grouped",
          display: "Agrupada",
          dataType: "multi",
        },
        {
          name: "bar-vertical-stacked",
          display: "Empilhada",
          dataType: "multi",
        },
        {
          name: "bar-vertical-normalized",
          display: "Normalizada",
          dataType: "multi",
        },
      ],
    },
    {
      name: "bars-horizontal",
      display: "Barras Horizontais",
      chartTypes: [
        { name: "bar-horizontal", display: "Padrão", dataType: "single" },
        {
          name: "bar-horizontal-grouped",
          display: "Agrupada",
          dataType: "multi",
        },
        {
          name: "bar-horizontal-stacked",
          display: "Empilhada",
          dataType: "multi",
        },
        {
          name: "bar-horizontal-normalized",
          display: "Normalizada",
          dataType: "multi",
        },
      ],
    },

    {
      name: "pies",
      display: "Pizza",
      chartTypes: [
        { name: "pie", display: "Padrão", dataType: "single" },
        { name: "pie-grid", display: "Grid", dataType: "single" },
        { name: "pie-advanced", display: "Avançado", dataType: "single" },
      ],
    },

    {
      name: "outros",
      display: "Outros",
      chartTypes: [
        { name: "heat-map", display: "Mapa de calor", dataType: "multi" },
        { name: "tree-map", display: "Árvore", dataType: "single" },
        { name: "cards", display: "Cartões", dataType: "single" },
        { name: "gauge", display: "Medidor", dataType: "single" },
      ],
    },
  ],
  colorScheme: [
    {
      display: "Vívido",
      scheme: {
        name: "vivid",
        selectable: true,
        group: "Ordinal",
        domain: [
          "#647c8a",
          "#3f51b5",
          "#2196f3",
          "#00b862",
          "#afdf0a",
          "#a7b61a",
          "#f3e562",
          "#ff9800",
          "#ff5722",
          "#ff4514",
        ],
      },
    },
    {
      display: "Natural",
      scheme: {
        name: "natural",
        selectable: true,
        group: "Ordinal",
        domain: [
          "#bf9d76",
          "#e99450",
          "#d89f59",
          "#f2dfa7",
          "#a5d7c6",
          "#7794b1",
          "#afafaf",
          "#707160",
          "#ba9383",
          "#d9d5c3",
        ],
      },
    },
    {
      display: "Legal",
      scheme: {
        name: "cool",

        selectable: true,
        group: "Ordinal",
        domain: [
          "#a8385d",
          "#7aa3e5",
          "#a27ea8",
          "#aae3f5",
          "#adcded",
          "#a95963",
          "#8796c0",
          "#7ed3ed",
          "#50abcc",
          "#ad6886",
        ],
      },
    },
    {
      display: "Fogo",
      scheme: {
        name: "fire",

        selectable: true,
        group: "Ordinal",
        domain: [
          "#ff3d00",
          "#bf360c",
          "#ff8f00",
          "#ff6f00",
          "#ff5722",
          "#e65100",
          "#ffca28",
          "#ffab00",
        ],
      },
    },
    {
      display: "Solar",
      scheme: {
        name: "solar",

        selectable: true,
        group: "Continuous",
        domain: [
          "#fff8e1",
          "#ffecb3",
          "#ffe082",
          "#ffd54f",
          "#ffca28",
          "#ffc107",
          "#ffb300",
          "#ffa000",
          "#ff8f00",
          "#ff6f00",
        ],
      },
    },
    {
      display: "Ar",
      scheme: {
        name: "air",

        selectable: true,
        group: "Continuous",
        domain: [
          "#e1f5fe",
          "#b3e5fc",
          "#81d4fa",
          "#4fc3f7",
          "#29b6f6",
          "#03a9f4",
          "#039be5",
          "#0288d1",
          "#0277bd",
          "#01579b",
        ],
      },
    },
    {
      display: "Àgua",
      scheme: {
        name: "aqua",

        selectable: true,
        group: "Continuous",
        domain: [
          "#e0f7fa",
          "#b2ebf2",
          "#80deea",
          "#4dd0e1",
          "#26c6da",
          "#00bcd4",
          "#00acc1",
          "#0097a7",
          "#00838f",
          "#006064",
        ],
      },
    },
    {
      display: "Chamas",
      scheme: {
        name: "flame",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#A10A28",
          "#D3342D",
          "#EF6D49",
          "#FAAD67",
          "#FDDE90",
          "#DBED91",
          "#A9D770",
          "#6CBA67",
          "#2C9653",
          "#146738",
        ],
      },
    },
    {
      display: "Oceano",
      scheme: {
        name: "ocean",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#1D68FB",
          "#33C0FC",
          "#4AFFFE",
          "#AFFFFF",
          "#FFFC63",
          "#FDBD2D",
          "#FC8A25",
          "#FA4F1E",
          "#FA141B",
          "#BA38D1",
        ],
      },
    },
    {
      display: "Floresta",
      scheme: {
        name: "forest",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#55C22D",
          "#C1F33D",
          "#3CC099",
          "#AFFFFF",
          "#8CFC9D",
          "#76CFFA",
          "#BA60FB",
          "#EE6490",
          "#C42A1C",
          "#FC9F32",
        ],
      },
    },
    {
      display: "Horizonte",
      scheme: {
        name: "horizon",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#2597FB",
          "#65EBFD",
          "#99FDD0",
          "#FCEE4B",
          "#FEFCFA",
          "#FDD6E3",
          "#FCB1A8",
          "#EF6F7B",
          "#CB96E8",
          "#EFDEE0",
        ],
      },
    },
    {
      display: "Neon",
      scheme: {
        name: "neons",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#FF3333",
          "#FF33FF",
          "#CC33FF",
          "#0000FF",
          "#33CCFF",
          "#33FFFF",
          "#33FF66",
          "#CCFF33",
          "#FFCC00",
          "#FF6600",
        ],
      },
    },
    {
      display: "Picnic",
      scheme: {
        name: "picnic",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#FAC51D",
          "#66BD6D",
          "#FAA026",
          "#29BB9C",
          "#E96B56",
          "#55ACD2",
          "#B7332F",
          "#2C83C9",
          "#9166B8",
          "#92E7E8",
        ],
      },
    },
    {
      display: "Noite",
      scheme: {
        name: "night",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#2B1B5A",
          "#501356",
          "#183356",
          "#28203F",
          "#391B3C",
          "#1E2B3C",
          "#120634",
          "#2D0432",
          "#051932",
          "#453080",
          "#75267D",
          "#2C507D",
          "#4B3880",
          "#752F7D",
          "#35547D",
        ],
      },
    },
    {
      display: "Luzes Noturna",
      scheme: {
        name: "nightLights",

        selectable: false,
        group: "Ordinal",
        domain: [
          "#4e31a5",
          "#9c25a7",
          "#3065ab",
          "#57468b",
          "#904497",
          "#46648b",
          "#32118d",
          "#a00fb3",
          "#1052a2",
          "#6e51bd",
          "#b63cc3",
          "#6c97cb",
          "#8671c1",
          "#b455be",
          "#7496c3",
        ],
      },
    },
  ],
  schemeType: [
    { name: "ordinal", display: "Ordinal" },
    { name: "linear", display: "Linear" },
  ],
  theme: [
    { name: "", display: "Padrão" },
    { name: "dark", display: "Dark" },
  ],
};
