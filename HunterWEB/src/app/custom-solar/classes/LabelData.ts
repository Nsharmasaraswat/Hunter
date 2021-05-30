export class LabelData {
    inflamabilidade: string;
    recomendacoesEspeciais: string;
    reatividade: string;
    riscoAVida: string;
    destino: string;
    loteInterno: string;
    produto: string;
    fornecedor: string;
    unidademedida: string;
    qtdRecebimento: number;
    qtdPorVolume: number;
    qtdVolume: number;
    qtdImpresso: number;
    nfEntrada: string;
    sku: string;
    loteFornecedor: string;
    dtRecebimento: string;
    dtFabricacao: string;
    dtValidade: string;
    rodape: string;

    constuctor() {
        this.inflamabilidade = '';
        this.recomendacoesEspeciais = '';
        this.reatividade = '';
        this.riscoAVida = '';
        this.destino = '';
        this.loteInterno = '';
        this.produto = '';
        this.fornecedor = '';
        this.unidademedida = '';
        this.qtdRecebimento = 0;
        this.qtdPorVolume = 0;
        this.qtdVolume = 0;
        this.qtdImpresso = 0;
        this.nfEntrada = '';
        this.sku = '';
        this.loteFornecedor = '';
        this.dtRecebimento = '';
        this.dtFabricacao = '';
        this.dtValidade = '';
        this.rodape = '';
    }
}