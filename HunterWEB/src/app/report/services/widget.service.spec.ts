import { TestBed, inject } from '@angular/core/testing';

import { WidgetService } from './widget.service';

describe('ReportService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WidgetService]
    });
  });

  it('should be created', inject([WidgetService], (service: WidgetService) => {
    expect(service).toBeTruthy();
  }));
});
