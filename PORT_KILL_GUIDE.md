# Port ve Process Yönetimi (Linux)

## Belirli bir portu kullanan process'i bulma

```bash
lsof -i :8081 -P -n
```

```bash
ss -lptn 'sport = :8081'
```

## Tüm dinleyen portları görme

```bash
ss -lptn
```

## Process'i sonlandırma (kill)

Örnek:

```bash
kill 85729
```

## Kill sonrası portun boş olduğunu doğrulama

```bash
ss -lptn 'sport = :8081'
```

```bash
lsof -i :8081 -P -n
```

## Tek satırda kill + doğrulama

```bash
kill 85729 && sleep 1 && ss -lptn 'sport = :8081' && echo '---' && lsof -i :8081 -P -n || true
```
