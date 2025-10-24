import { Injectable } from '@nestjs/common';

@Injectable()
export class CurrenciesService {
  private readonly cryptoCurrencies = [
    {
      id: 'BTC',
      name: 'Bitcoin',
      symbol: 'BTC',
    },
    {
      id: 'ETH',
      name: 'Ethereum',
      symbol: 'ETH',
    },
    {
      id: 'XRP',
      name: 'XRP',
      symbol: 'XRP',
    },
    {
      id: 'BCH',
      name: 'Bitcoin Cash',
      symbol: 'BCH',
    },
    {
      id: 'LTC',
      name: 'Litecoin',
      symbol: 'LTC',
    },
    {
      id: 'EOS',
      name: 'EOS',
      symbol: 'EOS',
    },
    {
      id: 'BNB',
      name: 'Binance Coin',
      symbol: 'BNB',
    },
    {
      id: 'LINK',
      name: 'Chainlink',
      symbol: 'LINK',
    },
    {
      id: 'NEO',
      name: 'NEO',
      symbol: 'NEO',
    },
    {
      id: 'ETC',
      name: 'Ethereum Classic',
      symbol: 'ETC',
    },
    {
      id: 'ONT',
      name: 'Ontology',
      symbol: 'ONT',
    },
    {
      id: 'CRO',
      name: 'Crypto.com Chain',
      symbol: 'CRO',
    },
    {
      id: 'CUC',
      name: 'Cucumber',
      symbol: 'CUC',
    },
    {
      id: 'USDC',
      name: 'USD Coin',
      symbol: 'USDC',
    },
  ];

  private readonly fiatCurrencies = [
    {
      id: 'SGD',
      name: 'Singapore Dollar',
      symbol: '$',
      code: 'SGD',
    },
    {
      id: 'EUR',
      name: 'Euro',
      symbol: '€',
      code: 'EUR',
    },
    {
      id: 'GBP',
      name: 'British Pound',
      symbol: '£',
      code: 'GBP',
    },
    {
      id: 'HKD',
      name: 'Hong Kong Dollar',
      symbol: '$',
      code: 'HKD',
    },
    {
      id: 'JPY',
      name: 'Japanese Yen',
      symbol: '¥',
      code: 'JPY',
    },
    {
      id: 'AUD',
      name: 'Australian Dollar',
      symbol: '$',
      code: 'AUD',
    },
    {
      id: 'USD',
      name: 'United States Dollar',
      symbol: '$',
      code: 'USD',
    },
  ];

  findAll(type: string | undefined) {
    if (type === 'crypto') {
      return this.cryptoCurrencies;
    }
    if (type === 'fiat') {
      return this.fiatCurrencies;
    }
    return [...this.cryptoCurrencies, ...this.fiatCurrencies];
  }

  search(keyword: string) {
    const allCurrencies = [...this.cryptoCurrencies, ...this.fiatCurrencies];
    const lowerCaseKeyword = keyword.toLowerCase();

    return allCurrencies.filter((currency) => {
      // Coin's name starts with the search term
      return (
        currency.name.toLowerCase().startsWith(lowerCaseKeyword) ||
        // Coin's name contains a partial match with a '' (space) prefixed to the search term
        currency.name.toLowerCase().includes(` ${lowerCaseKeyword}`) ||
        // Coin's symbol starts with the search term
        currency.symbol.toLowerCase().startsWith(lowerCaseKeyword)
      );
    });
  }
}
