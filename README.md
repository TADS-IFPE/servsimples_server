Como configurar o ServSimples-Server

- Pre-requisitos
> Ter o MySQL instalado
> Ter o Intellij ultimate instalado

- Clone o repo em um diretório de sua preferência 
> git clone https://github.com/TADS-IFPE/servsimples_server.git

- Configure o banco de dados
> Abra o terminal e acesse o Mysql e execute os comandos abaixo
> CREATE DATABASE servsimples;
> USE servsimples;
> CREATE USER 'ifpe'@'localhost' IDENTIFIED BY 'ifpe';
> GRANT ALL PRIVILEGES ON servsimples.* TO 'ifpe'@'localhost';

- Rode a aplicação.
