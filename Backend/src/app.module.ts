import { Module } from '@nestjs/common';
import { CurrenciesController } from './currencies.controller';
import { CurrenciesService } from './currencies.service';

@Module({
  imports: [],
  controllers: [CurrenciesController],
  providers: [CurrenciesService],
})
export class AppModule {}
