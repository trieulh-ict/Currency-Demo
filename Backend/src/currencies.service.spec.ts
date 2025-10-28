import { Test, TestingModule } from '@nestjs/testing';
import { CurrenciesService } from './currencies.service';

describe('CurrenciesService', () => {
  let service: CurrenciesService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CurrenciesService],
    }).compile();

    service = module.get<CurrenciesService>(CurrenciesService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  describe('findAll', () => {
    it('should return all currencies when type is not provided', () => {
      const result = service.findAll(undefined);
      expect(result.length).toBe(21);
    });

    it('should return crypto currencies when type is crypto', () => {
      const result = service.findAll('crypto');
      expect(result.length).toBe(14);
    });

    it('should return fiat currencies when type is fiat', () => {
      const result = service.findAll('fiat');
      expect(result.length).toBe(7);
    });
  });

  describe('search', () => {
    it('matches currencies whose name starts with the keyword ignoring case', () => {
      const result = service.search('bit');
      expect(result.map((currency) => currency.id)).toEqual(['BTC', 'BCH']);
    });

    it('matches currencies by subsequent words in the name', () => {
      const result = service.search('cash');
      expect(result.map((currency) => currency.id)).toEqual(['BCH']);
    });

    it('matches currencies by symbol prefix', () => {
      const result = service.search('usdc');
      expect(result.map((currency) => currency.id)).toEqual(['USDC']);
    });

    it('returns an empty array when there are no matches', () => {
      const result = service.search('zzz');
      expect(result).toEqual([]);
    });
  });
});
