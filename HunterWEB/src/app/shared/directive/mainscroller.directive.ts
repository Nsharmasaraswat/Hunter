import { Directive, ElementRef } from '@angular/core';

@Directive({
  selector: '[main-scroller]'
})
export class MainScrollerDirective {
  constructor(private el: ElementRef) {

  }

  public scrollToBottom(): void {
    //this.el.nativeElement.scrollTop = this.el.nativeElement.scrollHeight;
    this.el.nativeElement.scroll({
      top: this.el.nativeElement.scrollHeight,
      left: 0,
      behavior: 'smooth'
    });
  }
}