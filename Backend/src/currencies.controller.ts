import { Controller, Get, Query } from '@nestjs/common';
import { CurrenciesService } from './currencies.service';

@Controller('currencies')
export class CurrenciesController {
  constructor(private readonly currenciesService: CurrenciesService) {}

  @Get()
  findAll(@Query('type') type: string) {
    return this.currenciesService.findAll(type);
  }
}
