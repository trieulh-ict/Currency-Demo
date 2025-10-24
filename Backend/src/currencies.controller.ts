import { Controller, Get, Post, Query, Body } from '@nestjs/common';
import { CurrenciesService } from './currencies.service';
import { SearchCurrencyDto } from './dto/search-currency.dto';

@Controller('currencies')
export class CurrenciesController {
  constructor(private readonly currenciesService: CurrenciesService) {}

  @Get()
  findAll(@Query('type') type: string) {
    return this.currenciesService.findAll(type);
  }

  @Post('search')
  search(@Body() searchCurrencyDto: SearchCurrencyDto) {
    return this.currenciesService.search(searchCurrencyDto.keyword);
  }
}
