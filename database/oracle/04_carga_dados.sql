-- ============================================================
-- Smart HAS - Fase 6 | 04_carga_dados.sql
-- Carga de dados simulados: usuarios, unidades, dispositivos IoT,
-- leituras manuais e leituras de sensor (via procedure).
-- As datas sao relativas a SYSDATE para que os indicadores dos
-- "ultimos 30 dias" sempre tenham dados na demonstracao.
-- Pre-requisito: 01, 02 e 03 executados.
-- ============================================================
SET SERVEROUTPUT ON;

-- ---------- Usuarios (senhas em BCrypt, as mesmas da API) ----------
-- admin@smarthas.com / admin123   |   demais usuarios / 123456
INSERT INTO T_SHAS_USUARIO (NM_COMPLETO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Administrador Smart HAS', 'admin@smarthas.com',
        '$2a$10$Ej8NaB/kHHNxH3P/MAvrUeiqzNcbAuJxSeAlqNPsvStxNRREf3.7C', 'ADMIN');
INSERT INTO T_SHAS_USUARIO (NM_COMPLETO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Paciente Demonstracao', 'paciente@smarthas.com',
        '$2a$10$cs/VXahF2/vxLQ2DCxaUeOn5xu5KpJOjEabyW8PMI99Dm7i3trTvi', 'USER');
INSERT INTO T_SHAS_USUARIO (NM_COMPLETO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Maria Aparecida Souza', 'maria.souza@smarthas.com',
        '$2a$10$cs/VXahF2/vxLQ2DCxaUeOn5xu5KpJOjEabyW8PMI99Dm7i3trTvi', 'USER');
INSERT INTO T_SHAS_USUARIO (NM_COMPLETO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Joao Carlos Lima', 'joao.lima@smarthas.com',
        '$2a$10$cs/VXahF2/vxLQ2DCxaUeOn5xu5KpJOjEabyW8PMI99Dm7i3trTvi', 'USER');
INSERT INTO T_SHAS_USUARIO (NM_COMPLETO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Ana Beatriz Rocha', 'ana.rocha@smarthas.com',
        '$2a$10$cs/VXahF2/vxLQ2DCxaUeOn5xu5KpJOjEabyW8PMI99Dm7i3trTvi', 'USER');
INSERT INTO T_SHAS_USUARIO (NM_COMPLETO, DS_EMAIL, DS_SENHA_HASH, TP_PERFIL)
VALUES ('Roberto Ferreira', 'roberto.ferreira@smarthas.com',
        '$2a$10$cs/VXahF2/vxLQ2DCxaUeOn5xu5KpJOjEabyW8PMI99Dm7i3trTvi', 'USER');

-- ---------- Unidades de saude ----------
INSERT INTO T_SHAS_UNIDADE_SAUDE (NM_UNIDADE, TP_UNIDADE, VL_LATITUDE, VL_LONGITUDE, DS_ENDERECO)
VALUES ('Hospital das Clinicas', 'HOSPITAL', -23.555800, -46.669600, 'Av. Dr. Eneas de Carvalho Aguiar, 255');
INSERT INTO T_SHAS_UNIDADE_SAUDE (NM_UNIDADE, TP_UNIDADE, VL_LATITUDE, VL_LONGITUDE, DS_ENDERECO)
VALUES ('UBS Vila Mariana', 'CLINIC', -23.589000, -46.634000, 'Rua Sena Madureira, 1000');
INSERT INTO T_SHAS_UNIDADE_SAUDE (NM_UNIDADE, TP_UNIDADE, VL_LATITUDE, VL_LONGITUDE, DS_ENDERECO)
VALUES ('Sensor IoT - Praca da Se', 'SENSOR', -23.550500, -46.633300, 'Praca da Se, s/n');
INSERT INTO T_SHAS_UNIDADE_SAUDE (NM_UNIDADE, TP_UNIDADE, VL_LATITUDE, VL_LONGITUDE, DS_ENDERECO)
VALUES ('Hospital Regional de Osasco', 'HOSPITAL', -23.532500, -46.791700, 'Rua Ari Barroso, 355 - Osasco');
INSERT INTO T_SHAS_UNIDADE_SAUDE (NM_UNIDADE, TP_UNIDADE, VL_LATITUDE, VL_LONGITUDE, DS_ENDERECO, ST_ATIVO)
VALUES ('UBS Centro (em reforma)', 'CLINIC', -23.545000, -46.640000, 'Rua Xavier de Toledo, 50', 0);

-- ---------- Dispositivos IoT ----------
INSERT INTO T_SHAS_DISPOSITIVO (ID_USUARIO, CD_SERIE, DS_MODELO)
SELECT ID_USUARIO, 'SHAS-BP-0001', 'Omron HEM-7156T (Bluetooth)' FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'paciente@smarthas.com';
INSERT INTO T_SHAS_DISPOSITIVO (ID_USUARIO, CD_SERIE, DS_MODELO)
SELECT ID_USUARIO, 'SHAS-BP-0002', 'G-Tech BSP11 (Wi-Fi)' FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'maria.souza@smarthas.com';
INSERT INTO T_SHAS_DISPOSITIVO (ID_USUARIO, CD_SERIE, DS_MODELO)
SELECT ID_USUARIO, 'SHAS-BP-0003', 'Omron HEM-7156T (Bluetooth)' FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'joao.lima@smarthas.com';
INSERT INTO T_SHAS_DISPOSITIVO (ID_USUARIO, CD_SERIE, DS_MODELO, ST_ATIVO)
SELECT ID_USUARIO, 'SHAS-BP-0004', 'Multilaser HC204 (descontinuado)', 0 FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'ana.rocha@smarthas.com';

-- ---------- Leituras manuais (historico registrado pelo app) ----------
-- Bloco anonimo com sub-rotina local: insere o historico de forma enxuta.
-- (Roberto Ferreira fica sem leituras para demonstrar o caso "SEM DADOS")
DECLARE
  v_qtd PLS_INTEGER := 0;

  PROCEDURE add_leitura (p_email VARCHAR2, p_dias_atras NUMBER, p_hora VARCHAR2,
                         p_sis NUMBER, p_dia NUMBER, p_obs VARCHAR2) IS
  BEGIN
    INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO, TP_ORIGEM, DS_OBSERVACAO)
    SELECT ID_USUARIO, p_sis, p_dia,
           TO_TIMESTAMP(TO_CHAR(TRUNC(SYSDATE) - p_dias_atras, 'YYYY-MM-DD') || ' ' || p_hora,
                        'YYYY-MM-DD HH24:MI'),
           'MANUAL', p_obs
      FROM T_SHAS_USUARIO WHERE DS_EMAIL = p_email;
    v_qtd := v_qtd + SQL%ROWCOUNT;
  END add_leitura;
BEGIN
  add_leitura('paciente@smarthas.com', 12, '08:00', 118, 76, 'Em jejum');
  add_leitura('paciente@smarthas.com', 10, '09:30', 128, 84, 'Apos caminhada');
  add_leitura('paciente@smarthas.com', 8, '07:15', 145, 95, 'Dor de cabeca leve');
  add_leitura('paciente@smarthas.com', 6, '07:00', 150, 98, 'Manha agitada');
  add_leitura('paciente@smarthas.com', 4, '22:10', 138, 88, 'Antes de dormir');
  add_leitura('paciente@smarthas.com', 2, '07:10', 152, 96, 'Esqueceu a medicacao');
  add_leitura('maria.souza@smarthas.com', 9, '08:20', 116, 74, NULL);
  add_leitura('maria.souza@smarthas.com', 5, '08:05', 119, 78, NULL);
  add_leitura('maria.souza@smarthas.com', 1, '08:15', 121, 79, 'Pouco sono');
  add_leitura('joao.lima@smarthas.com', 7, '19:40', 162, 101, 'Stress no trabalho');
  add_leitura('joao.lima@smarthas.com', 3, '20:00', 184, 118, 'Tontura e visao turva');
  add_leitura('ana.rocha@smarthas.com', 6, '10:00', 132, 85, NULL);
  add_leitura('ana.rocha@smarthas.com', 2, '10:30', 126, 82, NULL);
  DBMS_OUTPUT.PUT_LINE(v_qtd || ' leituras manuais inseridas.');
END;
/
COMMIT;

-- ---------- Avalia as leituras manuais (gera os alertas historicos) ----------
DECLARE
  v_id_alerta NUMBER;
  v_sev       VARCHAR2(10);
  v_gerados   PLS_INTEGER := 0;
BEGIN
  FOR m IN (SELECT ID_MEDICAO FROM T_SHAS_MEDICAO ORDER BY DT_MEDICAO) LOOP
    PRC_SHAS_REGISTRAR_ALERTA(m.ID_MEDICAO, v_id_alerta, v_sev);
    IF v_id_alerta IS NOT NULL THEN
      v_gerados := v_gerados + 1;
    END IF;
  END LOOP;
  COMMIT;
  DBMS_OUTPUT.PUT_LINE(v_gerados || ' alerta(s) gerado(s) a partir do historico.');
END;
/

-- ---------- Importa leituras simuladas dos sensores IoT ----------
DECLARE
  v_qtd NUMBER;
BEGIN
  FOR d IN (SELECT ID_DISPOSITIVO, CD_SERIE FROM T_SHAS_DISPOSITIVO WHERE ST_ATIVO = 1 ORDER BY ID_DISPOSITIVO) LOOP
    PRC_SHAS_SIMULAR_LEITURAS_SENSOR(d.ID_DISPOSITIVO, 8, 30, v_qtd);
    DBMS_OUTPUT.PUT_LINE(d.CD_SERIE || ': ' || v_qtd || ' leituras importadas.');
  END LOOP;
END;
/

-- Conferencia rapida
SELECT 'USUARIOS' TABELA, COUNT(*) QTD FROM T_SHAS_USUARIO UNION ALL
SELECT 'UNIDADES',  COUNT(*) FROM T_SHAS_UNIDADE_SAUDE UNION ALL
SELECT 'DISPOSITIVOS', COUNT(*) FROM T_SHAS_DISPOSITIVO UNION ALL
SELECT 'MEDICOES',  COUNT(*) FROM T_SHAS_MEDICAO UNION ALL
SELECT 'ALERTAS',   COUNT(*) FROM T_SHAS_ALERTA;
