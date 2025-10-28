import { Test, TestingModule } from '@nestjs/testing';
import { CurrenciesController } from './currencies.controller';
import { CurrenciesService } from './currencies.service';
import { SearchCurrencyDto } from './dto/search-currency.dto';

describe('CurrenciesController', () => {
  let controller: CurrenciesController;
  const mockCurrenciesService = {
    findAll: jest.fn(),
    search: jest.fn(),
  };

  beforeEach(async () => {
    mockCurrenciesService.findAll.mockReset();
    mockCurrenciesService.search.mockReset();

    const module: TestingModule = await Test.createTestingModule({
      controllers: [CurrenciesController],
      providers: [
        {
          provide: CurrenciesService,
          useValue: mockCurrenciesService,
        },
      ],
    }).compile();

    controller = module.get<CurrenciesController>(CurrenciesController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });

  describe('findAll', () => {
    it('delegates to the service with the incoming type', () => {
      mockCurrenciesService.findAll.mockReturnValue(['btc']);

      const result = controller.findAll('crypto');

      expect(mockCurrenciesService.findAll).toHaveBeenCalledWith('crypto');
      expect(result).toEqual(['btc']);
    });

    it('uses undefined when type is omitted', () => {
      controller.findAll(undefined as unknown as string);

      expect(mockCurrenciesService.findAll).toHaveBeenCalledWith(undefined);
    });
  });

  describe('search', () => {
    it('passes the keyword through to the service', () => {
      const dto: SearchCurrencyDto = { keyword: 'bit' };
      mockCurrenciesService.search.mockReturnValue(['Bitcoin']);

      const result = controller.search(dto);

      expect(mockCurrenciesService.search).toHaveBeenCalledWith('bit');
      expect(result).toEqual(['Bitcoin']);
    });
  });
});
