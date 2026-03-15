--
-- PostgreSQL database dump
--

\restrict Icv8Z8emTp65cH9C6l19j5IRIpQyrRxMcCOpB7rd6Adq8o46yGuoaeDh381Tfef

-- Dumped from database version 16.11 (Ubuntu 16.11-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 18.0 (Ubuntu 18.0-1.pgdg24.04+3)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: update_cash_register_totals(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_cash_register_totals() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Actualizar totales cuando se inserta o actualiza una transacción
    UPDATE public.cash_register
    SET 
        total_sales_cash = (
            SELECT COALESCE(SUM(amount_cash), 0)
            FROM public.cash_transactions
            WHERE cash_register_id = NEW.cash_register_id
            AND transaction_type = 'VENTA'
        ),
        total_sales_transfer = (
            SELECT COALESCE(SUM(amount_transfer), 0)
            FROM public.cash_transactions
            WHERE cash_register_id = NEW.cash_register_id
            AND transaction_type = 'VENTA'
        ),
        total_expenses = (
            SELECT COALESCE(SUM(total_amount), 0)
            FROM public.cash_transactions
            WHERE cash_register_id = NEW.cash_register_id
            AND transaction_type = 'GASTO'
        ),
        total_investments = (
            SELECT COALESCE(SUM(total_amount), 0)
            FROM public.cash_transactions
            WHERE cash_register_id = NEW.cash_register_id
            AND transaction_type = 'INVERSION'
        ),
        date_updated = CURRENT_TIMESTAMP
    WHERE cash_register_id = NEW.cash_register_id;
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_cash_register_totals() OWNER TO postgres;

--
-- Name: update_date_updated_column(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_date_updated_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.date_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_date_updated_column() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cash_register; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cash_register (
    cash_register_id bigint NOT NULL,
    cash_register_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id bigint,
    outlet_id bigint NOT NULL,
    enterprise_id bigint NOT NULL,
    opening_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    closing_date timestamp without time zone,
    opening_cash numeric(12,2) DEFAULT 0.00 NOT NULL,
    opening_transfer numeric(12,2) DEFAULT 0.00 NOT NULL,
    opening_total numeric(12,2) DEFAULT 0.00 NOT NULL,
    closing_cash numeric(12,2) DEFAULT 0.00,
    closing_transfer numeric(12,2) DEFAULT 0.00,
    closing_total numeric(12,2) DEFAULT 0.00,
    total_sales_cash numeric(12,2) DEFAULT 0.00 NOT NULL,
    total_sales_transfer numeric(12,2) DEFAULT 0.00 NOT NULL,
    total_expenses numeric(12,2) DEFAULT 0.00 NOT NULL,
    total_investments numeric(12,2) DEFAULT 0.00 NOT NULL,
    cash_difference numeric(12,2) DEFAULT 0.00,
    transfer_difference numeric(12,2) DEFAULT 0.00,
    status character varying(50) DEFAULT 'ABIERTA'::character varying NOT NULL,
    opening_notes text,
    closing_notes text,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_closing_date CHECK (((closing_date IS NULL) OR (closing_date >= opening_date))),
    CONSTRAINT chk_opening_positive CHECK (((opening_cash >= (0)::numeric) AND (opening_transfer >= (0)::numeric))),
    CONSTRAINT chk_status CHECK (((status)::text = ANY ((ARRAY['ABIERTA'::character varying, 'POR_CERRAR'::character varying, 'CERRADA'::character varying])::text[])))
);


ALTER TABLE public.cash_register OWNER TO postgres;

--
-- Name: TABLE cash_register; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.cash_register IS 'Registro de apertura y cierre de cajas por sucursal';


--
-- Name: COLUMN cash_register.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cash_register.status IS 'Estados: ABIERTA, POR_CERRAR, CERRADA';


--
-- Name: cash_register_cash_register_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cash_register_cash_register_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cash_register_cash_register_id_seq OWNER TO postgres;

--
-- Name: cash_register_cash_register_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cash_register_cash_register_id_seq OWNED BY public.cash_register.cash_register_id;


--
-- Name: cash_transactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cash_transactions (
    transaction_id bigint NOT NULL,
    transaction_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    cash_register_id bigint NOT NULL,
    invoice_id bigint,
    transaction_type character varying(50) NOT NULL,
    payment_method character varying(50) NOT NULL,
    amount_cash numeric(12,2) DEFAULT 0.00 NOT NULL,
    amount_transfer numeric(12,2) DEFAULT 0.00 NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    description text NOT NULL,
    reference_number character varying(100),
    recipient character varying(255),
    user_id bigint,
    transaction_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_amounts_positive CHECK (((amount_cash >= (0)::numeric) AND (amount_transfer >= (0)::numeric) AND (total_amount >= (0)::numeric))),
    CONSTRAINT chk_payment_method CHECK (((payment_method)::text = ANY ((ARRAY['EFECTIVO'::character varying, 'TRANSFERENCIA'::character varying, 'MIXTO'::character varying])::text[]))),
    CONSTRAINT chk_total_matches CHECK ((total_amount = (amount_cash + amount_transfer))),
    CONSTRAINT chk_transaction_type CHECK (((transaction_type)::text = ANY ((ARRAY['VENTA'::character varying, 'GASTO'::character varying, 'INVERSION'::character varying, 'RETIRO'::character varying, 'DEPOSITO'::character varying, 'AJUSTE'::character varying, 'APERTURA'::character varying])::text[])))
);


ALTER TABLE public.cash_transactions OWNER TO postgres;

--
-- Name: TABLE cash_transactions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.cash_transactions IS 'Todas las transacciones de dinero en la caja';


--
-- Name: COLUMN cash_transactions.transaction_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cash_transactions.transaction_type IS 'Tipos: VENTA, GASTO, INVERSION, RETIRO, DEPOSITO, AJUSTE, APERTURA';


--
-- Name: COLUMN cash_transactions.payment_method; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.cash_transactions.payment_method IS 'Métodos: EFECTIVO, TRANSFERENCIA, MIXTO';


--
-- Name: cash_transactions_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cash_transactions_transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cash_transactions_transaction_id_seq OWNER TO postgres;

--
-- Name: cash_transactions_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cash_transactions_transaction_id_seq OWNED BY public.cash_transactions.transaction_id;


--
-- Name: categories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categories (
    category_id bigint NOT NULL,
    category_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    category_name character varying(255),
    category_description character varying(255),
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.categories OWNER TO postgres;

--
-- Name: categories_category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categories_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categories_category_id_seq OWNER TO postgres;

--
-- Name: categories_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categories_category_id_seq OWNED BY public.categories.category_id;


--
-- Name: clients; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.clients (
    client_id bigint NOT NULL,
    client_uuid uuid DEFAULT public.uuid_generate_v4(),
    client_fullname character varying(255),
    client_address character varying(255),
    client_email character varying(255),
    client_cellphone character varying(50),
    client_typeid character varying(50),
    client_ci character varying(50),
    client_ruc character varying(50),
    client_gender character varying(50),
    client_status boolean,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    enterprise_id bigint
);


ALTER TABLE public.clients OWNER TO postgres;

--
-- Name: clients_client_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.clients_client_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.clients_client_id_seq OWNER TO postgres;

--
-- Name: clients_client_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.clients_client_id_seq OWNED BY public.clients.client_id;


--
-- Name: details; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.details (
    detail_id bigint NOT NULL,
    detail_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    detail_name character varying(255),
    detail_description character varying(255),
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.details OWNER TO postgres;

--
-- Name: details_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.details_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.details_detail_id_seq OWNER TO postgres;

--
-- Name: details_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.details_detail_id_seq OWNED BY public.details.detail_id;


--
-- Name: digital_certs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.digital_certs (
    digital_cert_id bigint NOT NULL,
    digital_cert_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    digital_cert_name character varying(255),
    digital_certificate bytea,
    digital_cert_password character varying(255),
    digital_cert_status boolean,
    digital_cert_expiration_date date,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    enterprise_id bigint
);


ALTER TABLE public.digital_certs OWNER TO postgres;

--
-- Name: digital_certs_digital_cert_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.digital_certs_digital_cert_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.digital_certs_digital_cert_id_seq OWNER TO postgres;

--
-- Name: digital_certs_digital_cert_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.digital_certs_digital_cert_id_seq OWNED BY public.digital_certs.digital_cert_id;


--
-- Name: email_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.email_config (
    email_id bigint NOT NULL,
    email_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_email character varying(255),
    smtp_host character varying(255),
    smtp_port character varying(20),
    username character varying(255),
    password character varying(255),
    tls_enabled boolean DEFAULT false,
    ssl_enabled boolean DEFAULT false,
    status_email boolean DEFAULT true,
    enterprise_id bigint,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    subject_email text,
    body_email text
);


ALTER TABLE public.email_config OWNER TO postgres;

--
-- Name: email_config_email_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.email_config_email_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.email_config_email_id_seq OWNER TO postgres;

--
-- Name: email_config_email_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.email_config_email_id_seq OWNED BY public.email_config.email_id;


--
-- Name: emitters; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.emitters (
    emitter_id bigint NOT NULL,
    emitter_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    razon_social character varying(255),
    nombre_comercial character varying(255),
    ruc character varying(50),
    dir_matriz character varying(255),
    cod_estab character varying(50),
    pto_emision character varying(50),
    emitter_status boolean,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ambiente character varying(50),
    enterprise_id bigint
);


ALTER TABLE public.emitters OWNER TO postgres;

--
-- Name: emitters_emitter_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.emitters_emitter_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.emitters_emitter_id_seq OWNER TO postgres;

--
-- Name: emitters_emitter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.emitters_emitter_id_seq OWNED BY public.emitters.emitter_id;


--
-- Name: enterprises; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.enterprises (
    enterprise_id bigint NOT NULL,
    enterprise_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    enterprise_name character varying(255),
    enterprise_owner_name character varying(255),
    enterprise_owner_identification character varying(100),
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    enterprise_status boolean DEFAULT true
);


ALTER TABLE public.enterprises OWNER TO postgres;

--
-- Name: enterprises_enterprise_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.enterprises_enterprise_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.enterprises_enterprise_id_seq OWNER TO postgres;

--
-- Name: enterprises_enterprise_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.enterprises_enterprise_id_seq OWNED BY public.enterprises.enterprise_id;


--
-- Name: invoice_detail; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invoice_detail (
    detail_id bigint NOT NULL,
    detail_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    quantity integer,
    description character varying(255),
    total_value numeric(12,2),
    total_value_without_tax numeric(12,2),
    unit_value numeric(12,2),
    unit_value_without_tax numeric(12,2),
    product_tax numeric(10,2),
    stock_product_id bigint,
    stock_outlet_id bigint,
    invoice_id bigint,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.invoice_detail OWNER TO postgres;

--
-- Name: invoice_detail_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.invoice_detail_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.invoice_detail_detail_id_seq OWNER TO postgres;

--
-- Name: invoice_detail_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.invoice_detail_detail_id_seq OWNED BY public.invoice_detail.detail_id;


--
-- Name: invoice_header; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invoice_header (
    invoice_id bigint NOT NULL,
    invoice_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    invoice_status character varying(50),
    invoice_tax numeric(10,2),
    invoice_date date,
    invoice_total numeric(12,2),
    invoice_subtotal numeric(12,2),
    invoice_discount numeric(12,2),
    payment_type character varying(100),
    sequential character varying(50),
    remission_guide character varying(100),
    access_key character varying(100),
    issue_point character varying(10),
    establishment character varying(10),
    invoice_type character varying(50),
    user_id bigint,
    client_id bigint,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    enterprise_id bigint
);


ALTER TABLE public.invoice_header OWNER TO postgres;

--
-- Name: invoice_header_invoice_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.invoice_header_invoice_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.invoice_header_invoice_id_seq OWNER TO postgres;

--
-- Name: invoice_header_invoice_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.invoice_header_invoice_id_seq OWNED BY public.invoice_header.invoice_id;


--
-- Name: invoice_temp_authorization; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.invoice_temp_authorization (
    temp_id bigint NOT NULL,
    temp_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    file_base64 text,
    access_code character varying(100),
    reception_status character varying(50),
    authorization_status character varying(50),
    enterprise_id bigint,
    outlet_id bigint,
    invoice_id bigint,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.invoice_temp_authorization OWNER TO postgres;

--
-- Name: invoice_temp_authorization_temp_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.invoice_temp_authorization_temp_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.invoice_temp_authorization_temp_id_seq OWNER TO postgres;

--
-- Name: invoice_temp_authorization_temp_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.invoice_temp_authorization_temp_id_seq OWNED BY public.invoice_temp_authorization.temp_id;


--
-- Name: outlets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.outlets (
    outlet_id bigint NOT NULL,
    outlet_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    outlet_city character varying(255),
    outlet_address character varying(255),
    outlet_name character varying(255),
    outlet_telf character varying(255),
    outlet_status boolean,
    enterprise_id bigint,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.outlets OWNER TO postgres;

--
-- Name: outlets_outlet_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.outlets_outlet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.outlets_outlet_id_seq OWNER TO postgres;

--
-- Name: outlets_outlet_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.outlets_outlet_id_seq OWNED BY public.outlets.outlet_id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.products (
    product_id bigint NOT NULL,
    product_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    product_name character varying(255),
    product_code character varying(255),
    product_description character varying(255),
    category_id bigint,
    detail_id bigint,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.products OWNER TO postgres;

--
-- Name: products_product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.products_product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.products_product_id_seq OWNER TO postgres;

--
-- Name: products_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.products_product_id_seq OWNED BY public.products.product_id;


--
-- Name: stock; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stock (
    stock_product_id bigint NOT NULL,
    stock_outlet_id bigint NOT NULL,
    stock_quantity double precision,
    stock_avalible boolean,
    unit_price double precision,
    pvp_price double precision,
    stock_max integer,
    stock_min integer,
    apply_tax boolean,
    tax_id bigint
);


ALTER TABLE public.stock OWNER TO postgres;

--
-- Name: taxes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.taxes (
    tax_id bigint NOT NULL,
    tax_uuid uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    tax_code character varying(255),
    tax_percentage character varying(50),
    code_sri integer,
    tax_value double precision,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.taxes OWNER TO postgres;

--
-- Name: taxes_tax_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.taxes_tax_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.taxes_tax_id_seq OWNER TO postgres;

--
-- Name: taxes_tax_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.taxes_tax_id_seq OWNED BY public.taxes.tax_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    user_uuid uuid DEFAULT public.uuid_generate_v4(),
    user_name character varying(255),
    user_lastname character varying(255),
    user_password character varying(255),
    user_gender character varying(50),
    user_ci character varying(50),
    user_ruc character varying(50),
    user_status boolean,
    date_created timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    user_firstname character varying(255),
    enterprise_id bigint,
    user_rol character varying(50)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: view_cash_transactions_detail; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_cash_transactions_detail AS
 SELECT ct.transaction_id,
    ct.transaction_uuid,
    cr.cash_register_id,
    o.outlet_name,
    ct.transaction_type,
    ct.payment_method,
    ct.amount_cash,
    ct.amount_transfer,
    ct.total_amount,
    ct.description,
    ct.reference_number,
    ct.recipient,
    (((u.user_firstname)::text || ' '::text) || (u.user_lastname)::text) AS registered_by,
    ct.transaction_date,
    ih.sequential AS invoice_number
   FROM ((((public.cash_transactions ct
     JOIN public.cash_register cr ON ((ct.cash_register_id = cr.cash_register_id)))
     JOIN public.outlets o ON ((cr.outlet_id = o.outlet_id)))
     LEFT JOIN public.users u ON ((ct.user_id = u.user_id)))
     LEFT JOIN public.invoice_header ih ON ((ct.invoice_id = ih.invoice_id)))
  ORDER BY ct.transaction_date DESC;


ALTER VIEW public.view_cash_transactions_detail OWNER TO postgres;

--
-- Name: view_current_cash_register; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_current_cash_register AS
 SELECT cr.cash_register_id,
    cr.cash_register_uuid,
    o.outlet_name,
    (((u.user_firstname)::text || ' '::text) || (u.user_lastname)::text) AS cashier_name,
    cr.opening_date,
    cr.status,
    (cr.opening_cash + cr.opening_transfer) AS opening_total,
    cr.total_sales_cash,
    cr.total_sales_transfer,
    (cr.total_sales_cash + cr.total_sales_transfer) AS total_sales,
    cr.total_expenses,
    cr.total_investments,
    (((((cr.opening_cash + cr.opening_transfer) + cr.total_sales_cash) + cr.total_sales_transfer) - cr.total_expenses) - cr.total_investments) AS expected_total,
    ((cr.opening_cash + cr.total_sales_cash) - ( SELECT COALESCE(sum(cash_transactions.amount_cash), (0)::numeric) AS "coalesce"
           FROM public.cash_transactions
          WHERE ((cash_transactions.cash_register_id = cr.cash_register_id) AND ((cash_transactions.transaction_type)::text = ANY ((ARRAY['GASTO'::character varying, 'INVERSION'::character varying, 'RETIRO'::character varying])::text[]))))) AS current_cash,
    ((cr.opening_transfer + cr.total_sales_transfer) - ( SELECT COALESCE(sum(cash_transactions.amount_transfer), (0)::numeric) AS "coalesce"
           FROM public.cash_transactions
          WHERE ((cash_transactions.cash_register_id = cr.cash_register_id) AND ((cash_transactions.transaction_type)::text = ANY ((ARRAY['GASTO'::character varying, 'INVERSION'::character varying, 'RETIRO'::character varying])::text[]))))) AS current_transfer
   FROM ((public.cash_register cr
     LEFT JOIN public.outlets o ON ((cr.outlet_id = o.outlet_id)))
     LEFT JOIN public.users u ON ((cr.user_id = u.user_id)))
  WHERE ((cr.status)::text = ANY ((ARRAY['ABIERTA'::character varying, 'POR_CERRAR'::character varying])::text[]));


ALTER VIEW public.view_current_cash_register OWNER TO postgres;

--
-- Name: view_productos_en_minimo; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_productos_en_minimo AS
 SELECT row_number() OVER () AS id,
    s.stock_outlet_id AS outlet_id,
    o.outlet_name,
    p.product_id,
    p.product_name,
    c.category_name,
    d.detail_name,
    s.stock_quantity,
    s.stock_min
   FROM ((((public.stock s
     JOIN public.outlets o ON ((o.outlet_id = s.stock_outlet_id)))
     JOIN public.products p ON ((p.product_id = s.stock_product_id)))
     LEFT JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.details d ON ((d.detail_id = p.detail_id)))
  WHERE (s.stock_quantity <= (s.stock_min)::double precision);


ALTER VIEW public.view_productos_en_minimo OWNER TO postgres;

--
-- Name: view_productos_mas_vendidos; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_productos_mas_vendidos AS
 SELECT ih.enterprise_id,
    row_number() OVER () AS id,
    id.stock_outlet_id AS outlet_id,
    p.product_id,
    p.product_name,
    c.category_name,
    d.detail_name,
    sum(id.quantity) AS total_cantidad_vendida,
    sum(id.total_value) AS total_vendido_usd
   FROM ((((public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
     JOIN public.products p ON ((p.product_id = id.stock_product_id)))
     LEFT JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.details d ON ((d.detail_id = p.detail_id)))
  GROUP BY ih.enterprise_id, id.stock_outlet_id, p.product_id, p.product_name, c.category_name, d.detail_name
  ORDER BY (sum(id.quantity)) DESC;


ALTER VIEW public.view_productos_mas_vendidos OWNER TO postgres;

--
-- Name: view_productos_mas_vendidos_dia; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_productos_mas_vendidos_dia AS
 SELECT ih.invoice_date AS dia,
    ih.enterprise_id,
    id.stock_outlet_id,
    p.product_id,
    p.product_name,
    c.category_name,
    d.detail_name,
    sum(id.quantity) AS cantidad_vendida_dia,
    sum(id.total_value) AS total_vendido_dia_usd
   FROM ((((public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
     JOIN public.products p ON ((p.product_id = id.stock_product_id)))
     LEFT JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.details d ON ((d.detail_id = p.detail_id)))
  WHERE (ih.invoice_date = CURRENT_DATE)
  GROUP BY ih.invoice_date, ih.enterprise_id, id.stock_outlet_id, p.product_id, p.product_name, c.category_name, d.detail_name
  ORDER BY (sum(id.quantity)) DESC;


ALTER VIEW public.view_productos_mas_vendidos_dia OWNER TO postgres;

--
-- Name: view_productos_mas_vendidos_mes; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_productos_mas_vendidos_mes AS
 SELECT row_number() OVER () AS id,
    date_trunc('month'::text, (ih.invoice_date)::timestamp with time zone) AS mes,
    ih.enterprise_id,
    id.stock_outlet_id,
    p.product_id,
    p.product_name,
    c.category_name,
    d.detail_name,
    sum(id.quantity) AS cantidad_vendida_mes,
    sum(id.total_value) AS total_vendido_mes_usd
   FROM ((((public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
     JOIN public.products p ON ((p.product_id = id.stock_product_id)))
     LEFT JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.details d ON ((d.detail_id = p.detail_id)))
  WHERE (ih.invoice_date >= date_trunc('month'::text, (CURRENT_DATE)::timestamp with time zone))
  GROUP BY (date_trunc('month'::text, (ih.invoice_date)::timestamp with time zone)), ih.enterprise_id, id.stock_outlet_id, p.product_id, p.product_name, c.category_name, d.detail_name
  ORDER BY (sum(id.quantity)) DESC;


ALTER VIEW public.view_productos_mas_vendidos_mes OWNER TO postgres;

--
-- Name: view_productos_vendidos_dia; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_productos_vendidos_dia AS
 SELECT ih.enterprise_id,
    id.stock_outlet_id AS outlet_id,
    ih.invoice_date AS dia,
    p.product_id,
    p.product_name,
    c.category_name,
    d.detail_name,
    sum(id.quantity) AS cantidad_vendida_dia,
    sum(id.total_value) AS total_vendido_dia_usd
   FROM ((((public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
     JOIN public.products p ON ((p.product_id = id.stock_product_id)))
     LEFT JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.details d ON ((d.detail_id = p.detail_id)))
  WHERE (ih.invoice_date = CURRENT_DATE)
  GROUP BY ih.enterprise_id, id.stock_outlet_id, ih.invoice_date, p.product_id, p.product_name, c.category_name, d.detail_name
  ORDER BY (sum(id.quantity)) DESC;


ALTER VIEW public.view_productos_vendidos_dia OWNER TO postgres;

--
-- Name: view_productos_vendidos_mes; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_productos_vendidos_mes AS
 SELECT ih.enterprise_id,
    id.stock_outlet_id AS outlet_id,
    date_trunc('month'::text, (ih.invoice_date)::timestamp with time zone) AS mes,
    p.product_id,
    p.product_name,
    c.category_name,
    d.detail_name,
    sum(id.quantity) AS cantidad_vendida_mes,
    sum(id.total_value) AS total_vendido_mes_usd
   FROM ((((public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
     JOIN public.products p ON ((p.product_id = id.stock_product_id)))
     LEFT JOIN public.categories c ON ((c.category_id = p.category_id)))
     LEFT JOIN public.details d ON ((d.detail_id = p.detail_id)))
  WHERE (ih.invoice_date >= date_trunc('month'::text, (CURRENT_DATE)::timestamp with time zone))
  GROUP BY ih.enterprise_id, id.stock_outlet_id, (date_trunc('month'::text, (ih.invoice_date)::timestamp with time zone)), p.product_id, p.product_name, c.category_name, d.detail_name
  ORDER BY (sum(id.quantity)) DESC;


ALTER VIEW public.view_productos_vendidos_mes OWNER TO postgres;

--
-- Name: view_total_vendido_dia; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_total_vendido_dia AS
 SELECT ih.enterprise_id,
    id.stock_outlet_id AS outlet_id,
    ih.invoice_date AS dia,
    sum(id.total_value) AS total_vendido_dia
   FROM (public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
  WHERE (ih.invoice_date = CURRENT_DATE)
  GROUP BY ih.enterprise_id, id.stock_outlet_id, ih.invoice_date;


ALTER VIEW public.view_total_vendido_dia OWNER TO postgres;

--
-- Name: view_total_vendido_mes; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_total_vendido_mes AS
 SELECT ih.enterprise_id,
    id.stock_outlet_id AS outlet_id,
    date_trunc('month'::text, (ih.invoice_date)::timestamp with time zone) AS mes,
    sum(id.total_value) AS total_vendido_mes
   FROM (public.invoice_detail id
     JOIN public.invoice_header ih ON ((ih.invoice_id = id.invoice_id)))
  WHERE (ih.invoice_date >= date_trunc('month'::text, (CURRENT_DATE)::timestamp with time zone))
  GROUP BY ih.enterprise_id, id.stock_outlet_id, (date_trunc('month'::text, (ih.invoice_date)::timestamp with time zone));


ALTER VIEW public.view_total_vendido_mes OWNER TO postgres;

--
-- Name: cash_register cash_register_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_register ALTER COLUMN cash_register_id SET DEFAULT nextval('public.cash_register_cash_register_id_seq'::regclass);


--
-- Name: cash_transactions transaction_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_transactions ALTER COLUMN transaction_id SET DEFAULT nextval('public.cash_transactions_transaction_id_seq'::regclass);


--
-- Name: categories category_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories ALTER COLUMN category_id SET DEFAULT nextval('public.categories_category_id_seq'::regclass);


--
-- Name: clients client_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients ALTER COLUMN client_id SET DEFAULT nextval('public.clients_client_id_seq'::regclass);


--
-- Name: details detail_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.details ALTER COLUMN detail_id SET DEFAULT nextval('public.details_detail_id_seq'::regclass);


--
-- Name: digital_certs digital_cert_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digital_certs ALTER COLUMN digital_cert_id SET DEFAULT nextval('public.digital_certs_digital_cert_id_seq'::regclass);


--
-- Name: email_config email_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_config ALTER COLUMN email_id SET DEFAULT nextval('public.email_config_email_id_seq'::regclass);


--
-- Name: emitters emitter_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emitters ALTER COLUMN emitter_id SET DEFAULT nextval('public.emitters_emitter_id_seq'::regclass);


--
-- Name: enterprises enterprise_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enterprises ALTER COLUMN enterprise_id SET DEFAULT nextval('public.enterprises_enterprise_id_seq'::regclass);


--
-- Name: invoice_detail detail_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_detail ALTER COLUMN detail_id SET DEFAULT nextval('public.invoice_detail_detail_id_seq'::regclass);


--
-- Name: invoice_header invoice_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_header ALTER COLUMN invoice_id SET DEFAULT nextval('public.invoice_header_invoice_id_seq'::regclass);


--
-- Name: invoice_temp_authorization temp_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_temp_authorization ALTER COLUMN temp_id SET DEFAULT nextval('public.invoice_temp_authorization_temp_id_seq'::regclass);


--
-- Name: outlets outlet_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.outlets ALTER COLUMN outlet_id SET DEFAULT nextval('public.outlets_outlet_id_seq'::regclass);


--
-- Name: products product_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products ALTER COLUMN product_id SET DEFAULT nextval('public.products_product_id_seq'::regclass);


--
-- Name: taxes tax_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.taxes ALTER COLUMN tax_id SET DEFAULT nextval('public.taxes_tax_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Data for Name: cash_register; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cash_register (cash_register_id, cash_register_uuid, user_id, outlet_id, enterprise_id, opening_date, closing_date, opening_cash, opening_transfer, opening_total, closing_cash, closing_transfer, closing_total, total_sales_cash, total_sales_transfer, total_expenses, total_investments, cash_difference, transfer_difference, status, opening_notes, closing_notes, date_created, date_updated) FROM stdin;
14	06e58702-5286-4fcf-b533-cbbce323b64a	1	1	2	2026-01-08 16:59:04.403468	2026-01-08 17:12:41.407398	30.00	10.00	40.00	161.00	30.00	191.00	0.00	0.00	0.00	0.00	131.00	20.00	CERRADA	INICIO DE APERTURA	CAJA CERRADA Y CAUDRADA TODO	2026-01-08 16:59:04.404624	2026-01-08 17:12:41.417272
15	c2e00621-e836-4ff3-815f-a262dbec5bc4	1	1	2	2026-01-08 17:15:16.746955	2026-01-09 12:27:46.357458	5.00	10.00	15.00	60.00	0.00	60.00	0.00	0.00	24.00	0.00	79.00	-10.00	CERRADA	ABIERTA DESDE LA TARDE		2026-01-08 17:15:16.747543	2026-01-09 12:27:46.369465
16	36b3aaa2-2885-4d02-9c6a-11466f5a0c48	1	1	2	2026-01-09 12:55:34.820638	2026-01-09 16:49:01.794205	20.00	1.00	21.00	39.00	25.25	64.25	26.50	24.50	7.75	0.00	0.25	-0.25	CERRADA	INICIO DE DIA	Se Cierra caja sin ninguna falta	2026-01-09 12:55:34.821088	2026-01-09 17:00:58.241178
17	9694c325-3482-4639-9d5a-dc18dd408fd0	1	1	2	2026-01-09 17:23:43.782359	2026-01-09 18:14:34.050365	20.00	10.00	30.00	62.00	13.00	75.00	45.00	3.00	2.00	0.00	-1.00	0.00	CERRADA	MITAD TARDE	CIERRE DE NOCHE	2026-01-09 17:23:43.790404	2026-01-09 18:14:34.247447
\.


--
-- Data for Name: cash_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cash_transactions (transaction_id, transaction_uuid, cash_register_id, invoice_id, transaction_type, payment_method, amount_cash, amount_transfer, total_amount, description, reference_number, recipient, user_id, transaction_date, date_created, date_updated) FROM stdin;
4	63d47187-ce84-4b35-8765-1361266755e2	15	\N	GASTO	EFECTIVO	5.50	5.50	11.00	ALMUERZOS	\N	JIFFY	1	2026-01-08 19:21:03.433868	2026-01-08 19:21:03.435314	2026-01-08 19:21:03.435367
5	82de5486-1cdd-4d78-b556-07ae1b47961f	15	\N	GASTO	EFECTIVO	1.00	1.00	2.00	AGUA	\N	VECINO CARPAS	1	2026-01-08 19:22:45.639619	2026-01-08 19:22:45.64112	2026-01-08 19:22:45.641212
6	1cb06085-fef6-40f0-b5ad-76155ba86b2e	15	\N	GASTO	EFECTIVO	5.50	5.50	11.00	almuerzos	\N	Jiffy	1	2026-01-08 19:38:03.245553	2026-01-08 19:38:03.247594	2026-01-08 19:38:03.247702
7	1e64374a-a779-4658-8a7b-93b712326050	16	\N	VENTA	MIXTO	7.50	8.50	16.00	Venta #N/A	CERRADO	Cliente	1	2026-01-09 15:32:32.635071	2026-01-09 15:32:32.635608	2026-01-09 17:00:50.744759
8	2703c094-67f7-40fb-990e-77ac8226c339	16	\N	VENTA	EFECTIVO	19.00	0.00	19.00	Venta #N/A	CERRADO	Cliente	1	2026-01-09 15:44:17.373411	2026-01-09 15:44:17.373782	2026-01-09 17:00:50.755281
9	0ef081c1-a857-4451-98db-e84e7f488c23	16	\N	VENTA	TRANSFERENCIA	0.00	16.00	16.00	Venta #N/A	CERRADO	Cliente	1	2026-01-09 15:44:51.728077	2026-01-09 15:44:51.728707	2026-01-09 17:00:50.75931
10	c4a98906-2701-4d91-b6f4-84252b79dffe	16	\N	GASTO	EFECTIVO	5.50	0.00	5.50	ALMUERZOS DE DOS	CERRADO	JIFFY	1	2026-01-09 15:57:34.18739	2026-01-09 15:57:34.187833	2026-01-09 17:00:50.762679
11	15699fd9-cbef-4edf-8c2d-3abd79d8de80	16	\N	GASTO	EFECTIVO	2.00	0.00	2.00	CHICLES Y SNACKS	CERRADO	VECI CARPAS	1	2026-01-09 16:03:33.111924	2026-01-09 16:03:33.112382	2026-01-09 17:00:50.766438
12	57506edc-30c1-4a5b-a9df-7189e2d7e3a8	16	\N	GASTO	TRANSFERENCIA	0.00	0.25	0.25	HELADO	CERRADO	VECINITO	1	2026-01-09 16:05:17.802195	2026-01-09 16:05:17.80302	2026-01-09 17:00:58.241178
15	f377d3e1-acb8-445f-b3f0-2c53a96dc963	17	\N	VENTA	EFECTIVO	22.00	0.00	22.00	Venta #N/A	CERRADO	Cliente	1	2026-01-09 18:12:41.475866	2026-01-09 18:12:41.477408	2026-01-09 18:14:34.231424
13	7a7fbdfc-201e-4b24-ae71-e185d3db0344	17	\N	GASTO	EFECTIVO	2.00	0.00	2.00	dos helados para mi esposita	CERRADO	VECINO CARPAS	1	2026-01-09 18:10:58.374426	2026-01-09 18:10:58.402555	2026-01-09 18:14:34.242501
16	e1516f59-34d3-475a-9f2d-9311f5d4975b	17	\N	VENTA	MIXTO	10.00	3.00	13.00	Venta #N/A	CERRADO	Cliente	1	2026-01-09 18:13:20.75085	2026-01-09 18:13:20.753505	2026-01-09 18:14:34.242383
14	ba4ec859-6ada-4443-983c-d96d58d3ee7d	17	\N	VENTA	EFECTIVO	13.00	0.00	13.00	Venta #N/A	CERRADO	Cliente	1	2026-01-09 18:11:15.183613	2026-01-09 18:11:15.185012	2026-01-09 18:14:34.247447
\.


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categories (category_id, category_uuid, category_name, category_description, date_created, date_updated) FROM stdin;
1	c9abe097-e7d3-4f30-a95d-d6e262481446	BASES HIDRATANTES	BASES LIQUIDAS HIDRATANTES	2025-09-15 14:36:25.156165	2025-09-15 14:36:25.156165
2	18bb313a-3bbd-4c98-b4e8-6a4d7345f5a4	CORRECTORES HIDRATANTES	CORRECTORES HIDRATANTES	2025-09-15 16:07:36.076971	2025-09-15 16:07:36.077014
9	e8924dc6-830e-4ea3-811e-d21db623e38d	Ropa	\N	2025-09-30 19:32:14.741043	2025-09-30 19:32:14.741113
10	2eabc096-043b-48fa-8549-aa1823d7c80c	GEL CEJAS	\N	2025-11-14 18:45:01.600268	2025-11-14 18:45:01.6003
11	dc705fa6-701c-4e32-8258-23ede7a4db2e	GLOSS	\N	2025-11-14 18:45:02.2671	2025-11-14 18:45:02.267141
12	e922155d-a2f6-4cf6-830b-4546d19087c7	BASE MATTE	\N	2025-11-14 18:45:12.516879	2025-11-14 18:45:12.516919
13	45a19d7d-3ddb-4515-aeaa-d7fb096feb0c	BASE	\N	2025-11-14 18:45:15.137052	2025-11-14 18:45:15.137082
14	ea201bb5-0979-4ad7-b5cb-fd218e2cfcbf	SKIN TINT	\N	2025-11-14 18:45:17.773967	2025-11-14 18:45:17.773978
15	0aa93881-c05a-48a3-9e93-08436577cc17	BASE + CORRECTOR	\N	2025-11-14 18:45:17.822032	2025-11-14 18:45:17.822048
16	c5fadaec-ec13-4059-aaa7-c6c51f02ccc7	CORRECTOR	\N	2025-11-14 18:45:18.275452	2025-11-14 18:45:18.275463
17	c7f3dc60-5593-4c03-a28c-4289c61db672	EYESHADOW	\N	2025-11-14 18:45:18.790967	2025-11-14 18:45:18.790973
18	77759142-dcd1-441e-9f03-64a4340a39b8	CONTOUR + BRONZER	\N	2025-11-14 18:45:18.883658	2025-11-14 18:45:18.883665
19	b3bf6772-999d-41d2-8963-0755a04b06a8	CONTOUR POWDER	\N	2025-11-14 18:45:18.92827	2025-11-14 18:45:18.928276
20	97bff0d7-9f81-4f91-a9ce-a87d1406f43e	CONTOUR + HIGHLIGHT POWDER	\N	2025-11-14 18:45:18.944867	2025-11-14 18:45:18.944873
21	91b10afc-f785-4814-bb48-f30ec02fd98c	BRONZER	\N	2025-11-14 18:45:18.961654	2025-11-14 18:45:18.96166
22	5dd4cdfb-57af-45eb-9622-0b8736b8fc42	HIGHLIGHT	\N	2025-11-14 18:45:18.978458	2025-11-14 18:45:18.978465
23	2ea4b6cb-02b2-4bdd-8458-a84d6b3be919	POLVO SUELTO	\N	2025-11-14 18:45:19.05276	2025-11-14 18:45:19.05277
24	c65ad690-647f-4fd0-bcfa-8f0da8fe0ba8	BASE EN POLVO	\N	2025-11-14 18:45:19.101517	2025-11-14 18:45:19.101523
25	5b771597-e68a-45ee-948a-6b3e1e7479b1	PRIMER	\N	2025-11-14 18:45:19.268356	2025-11-14 18:45:19.268384
26	c51f563e-5c31-401c-a0c4-1aab4071355e	BLUSH	\N	2025-11-14 18:45:19.299498	2025-11-14 18:45:19.299504
27	3a114cd6-fb06-445f-991e-a47866059d06	SIN DEFINICION	\N	2026-02-03 21:51:56.442478	2026-02-03 21:51:56.442512
28	8067c19f-4318-4ddb-b5a5-5629ea049c37	CAMISETA NEON	\N	2026-02-06 21:41:29.298319	2026-02-06 21:41:29.298444
\.


--
-- Data for Name: clients; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.clients (client_id, client_uuid, client_fullname, client_address, client_email, client_cellphone, client_typeid, client_ci, client_ruc, client_gender, client_status, date_created, date_updated, enterprise_id) FROM stdin;
1	ce94cf46-bc06-4dec-8a07-677b8b3c0506	CABASCANGO ANRANGO GISSEL VANESSA	FAUSTINO BURGA Y MANUEL ANDRADE	gisselvanessaa@gmail.com	0939516037	CI	1716247943	1716247943001	F	t	2025-08-14 18:32:36.045638	2025-11-06 13:20:42.152249	2
2	0eba3b80-4d52-4d59-ad08-dc54c7f0598f	CONSUMIDOR FINAL	CONSUMIDOR FINAL	consumidorfinal@consumidorfinal.com	09999999999	C	9999999999999	9999999999999	M	t	2025-09-23 09:10:02.520232	2025-12-01 18:12:03.636083	2
9	fa314a8b-de0b-4f2b-940e-832abfb22ab8	BRITANY VALENTINA DELGADO	EL ORO	gisselvanessaa@gmail.com	0994501275	CI	0750895575	0750895575	N/A	t	2025-12-12 15:18:27.468461	2025-12-12 15:18:27.468517	2
10	f5d1a829-9a41-4adc-9c4b-b8d110bc7029					CI			N/A	t	2025-12-12 17:47:02.345301	2025-12-12 17:47:02.345366	2
11	be16053b-a9c6-42ff-99eb-2b055a0f8155	XIMENA MISHELLE LOPEZ IBARRA	ESMERALDAS	solangepaola01@gmail.com	0994943146	CI	0850564121	0850564121	N/A	t	2026-02-13 14:23:02.503931	2026-02-13 14:23:02.503967	1
4	71cd4b05-22ce-43a6-89ba-7c06787e288d	QUINCHE MORAN LUIS ANDRES	FAUSTINO BURGA	laqm_14@hotmail.com	0994501275	CI	1003866173	1003866173001	M	t	2025-09-25 22:52:21.605393	2025-10-04 10:16:57.633715	2
8	3d9f4fdc-e7f7-4b86-a193-ee6767c1174e	SUQUITANA ORELLANA JENNIFER ELIZABETH	CUENCA	gisselvanessaa@gmail.com	0998591809	C	0107624520	0107624520001	NO DEFINIDO	t	2025-11-05 23:24:15.474972	2025-11-06 12:21:11.926466	2
5	907587cb-8a5b-4631-96f4-fb65b31691ac	ANRANGO VELASQUEZ MARIA MATILDE	OTAVALO	gisselvanessaa@gmail.com	0994501275	C	1001881828	1001881828001	NO DEFINIDO	t	2025-11-05 22:22:49.984854	2025-11-06 12:21:26.414127	2
6	a23aabc8-dfe3-4c5b-873c-7d22ba913e17	LEMA TERAN RAFAEL	OTAVALO	gisselvanessaa@gmail.com	0994501275	C	1002899936	1002899936001	NO DEFINIDO	t	2025-11-05 23:07:43.44205	2025-11-06 12:21:26.48265	2
7	2adb0104-e282-4786-aa20-8da0b80c4b29	QUINCHE REMACHE LUIS ENRIQUE	OTAVALO	laqm_14@hotmail.com	0994501275	C	1001030210	1001030210001	NO DEFINIDO	t	2025-11-05 23:10:58.060804	2025-11-06 12:21:26.535839	2
\.


--
-- Data for Name: details; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.details (detail_id, detail_uuid, detail_name, detail_description, date_created, date_updated) FROM stdin;
1	b15bd5cf-67e1-4ac3-89db-e01b2fdaca13	Light Beige	Light Beige	2025-09-15 16:19:09.63807	2025-09-15 16:19:44.9326
2	c6bda532-ffb9-429d-bc53-3dd1a2cf1a57	Dusty Rose	Dusty Rose	2025-09-15 16:22:29.612242	2025-09-15 16:24:00.751225
3	1a135846-4255-43ae-89e3-7f18670e7a7d	Natural Beige	Natural Beige - Tono	2025-09-15 16:25:08.55393	2025-09-15 16:25:08.553953
4	6feaaae9-3b0d-482c-ab69-df145a65e460	220	Tono 220	2025-09-15 16:26:19.582523	2025-09-15 16:26:19.582589
5	20e9fcf4-4ad2-4ff7-8fdf-572389cc2c3e	0.5	\N	2025-09-15 17:36:48.513395	2025-09-15 17:36:48.513418
18	26e94728-e49b-46cd-9eb5-b12570c64e52	Camiseta	\N	2025-09-30 19:32:14.79322	2025-09-30 19:32:14.793289
19	955fe6bc-be71-44d2-9335-047a152bf88d	Pantalón	\N	2025-09-30 19:32:14.858934	2025-09-30 19:32:14.858975
20	f5d3a344-7ca3-4e66-bd3c-71fa6e09a663	MINI	\N	2025-11-14 18:45:01.708539	2025-11-14 18:45:01.708566
21	32d2d070-23a5-453c-8fe5-fec566b19e35	FULL SIZE	\N	2025-11-14 18:45:02.058229	2025-11-14 18:45:02.058286
22	0fd0fcef-3de1-4056-8757-dc3a84b55689	MONEY MAUVE	\N	2025-11-14 18:45:02.331262	2025-11-14 18:45:02.331324
23	d298fe81-fafe-4a71-bd15-68f6318ef904	JAM SESSION	\N	2025-11-14 18:45:02.546448	2025-11-14 18:45:02.546488
24	203972a5-c6a0-4e00-a183-737eaa9fc5ee	CHOCOLUXE	\N	2025-11-14 18:45:02.759087	2025-11-14 18:45:02.759183
25	83a03670-35ca-49b8-ac23-d6c141410604	DIVINE WINE	\N	2025-11-14 18:45:02.972109	2025-11-14 18:45:02.972171
26	00fcdee9-540d-47df-9fa7-70090bd5e4fe	CORAL FIXATION	\N	2025-11-14 18:45:03.173076	2025-11-14 18:45:03.173135
27	cdcb50b1-9c07-4813-b26f-93e1550eb8f1	JELLY POP	\N	2025-11-14 18:45:03.376581	2025-11-14 18:45:03.376665
28	7987517b-f1fa-4089-806d-487c3a60b73a	PRINCESS CUT	\N	2025-11-14 18:45:03.570225	2025-11-14 18:45:03.57032
29	af665f9a-5260-4b09-9220-429b39f78c65	PINK QUARTZ	\N	2025-11-14 18:45:03.754879	2025-11-14 18:45:03.75494
30	2a5bd876-1afa-4be1-be2d-bbf106ab7e51	ITS GIVING GUAVA	\N	2025-11-14 18:45:03.949462	2025-11-14 18:45:03.949525
31	cd3d330d-c30d-4ea0-9d8d-1521d2cf859d	OPAL OGY TOUR	\N	2025-11-14 18:45:04.140223	2025-11-14 18:45:04.1403
32	4fcfc119-0f8a-481f-8622-1be50fdb660f	RED DELICIOUS	\N	2025-11-14 18:45:04.322682	2025-11-14 18:45:04.322737
33	18e1b9db-1a0b-4d8c-9b03-2df8242b059b	CITRINE GLEAM	\N	2025-11-14 18:45:04.496574	2025-11-14 18:45:04.496599
34	1415f147-dd30-4a5e-a926-c3a87be35886	CRYSTAL BALLER	\N	2025-11-14 18:45:04.677686	2025-11-14 18:45:04.677706
35	ad472911-36d9-4d8f-8c05-bc6756077e45	CANDY CODED	\N	2025-11-14 18:45:04.833999	2025-11-14 18:45:04.834019
36	c74fc188-9da7-4c97-bc61-4ea3c048ada3	SUPER NEUTRAL	\N	2025-11-14 18:45:05.135327	2025-11-14 18:45:05.135432
37	954977ed-d8c4-4920-baeb-5c7f347ec245	MISSED CALL	\N	2025-11-14 18:45:05.319753	2025-11-14 18:45:05.319844
38	22c815db-593e-4908-9a11-a352c1a2b1c1	FOLLOW BACK	\N	2025-11-14 18:45:05.506016	2025-11-14 18:45:05.506074
39	4746d4e3-62e3-4c7a-a642-595bf5966506	THATS CHIC	\N	2025-11-14 18:45:05.689581	2025-11-14 18:45:05.689604
40	3f4bc888-743c-4d6a-91ef-8462a4542946	STATUS UPDATE	\N	2025-11-14 18:45:05.861389	2025-11-14 18:45:05.861407
41	d476ae9b-d823-460c-84c0-4afd23082c89	NEWSPEED	\N	2025-11-14 18:45:06.025105	2025-11-14 18:45:06.025196
42	16ea7ecd-f5c1-401f-b4a2-2cd3e835391f	SUPERMODEL	\N	2025-11-14 18:45:06.215157	2025-11-14 18:45:06.215223
43	65d8fbda-0ca2-40db-a7a5-965ff2e3362d	007 AMBER	\N	2025-11-14 18:45:06.456554	2025-11-14 18:45:06.456616
44	1693cba9-5022-4298-93ac-789022a6f15a	006 REEF	\N	2025-11-14 18:45:06.745255	2025-11-14 18:45:06.745315
45	f1fbb2eb-3e94-41bc-b696-6eb8379065d8	009 TOPAZ	\N	2025-11-14 18:45:07.029146	2025-11-14 18:45:07.029168
46	a20db968-d340-4c13-82bc-bbbb007522ea	024 BUBBLEGUM	\N	2025-11-14 18:45:07.208576	2025-11-14 18:45:07.208659
47	cd2a53f5-fe2c-4685-b534-baca37ab58df	004 SILK	\N	2025-11-14 18:45:07.389211	2025-11-14 18:45:07.389265
48	e2494352-edca-4fb5-926c-ef52d97d17f0	026 HONEY	\N	2025-11-14 18:45:07.579774	2025-11-14 18:45:07.579876
49	6f9cead8-a480-4b80-8a3f-c6e31d02c191	023 SWEETHEART	\N	2025-11-14 18:45:07.763024	2025-11-14 18:45:07.763064
50	2b333f97-f3cc-4c36-85a0-895bb5dda378	006 HOTCHILI	\N	2025-11-14 18:45:07.944689	2025-11-14 18:45:07.944713
51	a14c02d2-41d6-4b86-bc39-9acf41f1b8d9	007 COCOAZING	\N	2025-11-14 18:45:08.124853	2025-11-14 18:45:08.124887
52	ec1af657-6380-4917-86ac-c45df14e8f10	005 PEACHFEVER	\N	2025-11-14 18:45:08.304287	2025-11-14 18:45:08.304327
53	acb30b1c-6fa4-4db4-8036-3d9824f19503	008 HOTHONEY	\N	2025-11-14 18:45:08.487337	2025-11-14 18:45:08.487487
54	669b5c45-420d-47aa-8735-3e7071f9ac1f	TONO 35	\N	2025-11-14 18:45:08.679029	2025-11-14 18:45:08.679043
55	7f35d3fa-a30f-44cd-bcb0-f582195c4819	TONO 05	\N	2025-11-14 18:45:08.848752	2025-11-14 18:45:08.848773
56	fed06f43-6618-4420-a5d5-82f7fa5fb0a6	TONO 33	\N	2025-11-14 18:45:09.046027	2025-11-14 18:45:09.046067
57	876431db-d2f9-4584-9331-5d03d6aecfb6	TONO 31	\N	2025-11-14 18:45:09.21178	2025-11-14 18:45:09.211792
58	107665b0-8593-4835-a5df-8d1954932476	TONO 27	\N	2025-11-14 18:45:09.388682	2025-11-14 18:45:09.388711
59	19edeaba-cc57-45c3-84d9-6f8d54b0fda4	TONO 26	\N	2025-11-14 18:45:09.558644	2025-11-14 18:45:09.558696
60	d3a67f81-5731-4893-a2cc-986512d3d751	TONO 01	\N	2025-11-14 18:45:09.74777	2025-11-14 18:45:09.747789
61	103af442-948d-424c-80cc-f2d7828f399b	104 POPPIN POMEGRANATE	\N	2025-11-14 18:45:09.923126	2025-11-14 18:45:09.923141
62	5775e15c-2291-4a38-ab13-fa7853f6c91b	101 LOVELY LITCHI	\N	2025-11-14 18:45:10.12029	2025-11-14 18:45:10.12033
63	c884b8e2-6113-4bd1-92fa-74828f1271ba	102 WITTY WATERMELON	\N	2025-11-14 18:45:10.291603	2025-11-14 18:45:10.291617
64	5e4e3040-020b-45ea-8110-0848566ccf45	103 PROUD PAPAYA	\N	2025-11-14 18:45:10.482582	2025-11-14 18:45:10.482622
65	7360e538-ffb5-46c6-801e-de077492c9e3	01 CAKE MY DAY	\N	2025-11-14 18:45:10.667192	2025-11-14 18:45:10.667226
66	bb4df501-d96c-42bc-a70a-8a91e71aa2b5	AS IF	\N	2025-11-14 18:45:10.844098	2025-11-14 18:45:10.844116
67	97ee15b8-5bb9-43f6-8e17-b485cdb2f2a2	THATS HOT	\N	2025-11-14 18:45:10.997024	2025-11-14 18:45:10.997048
68	3d226e0c-4597-4a74-b328-ff2f40f6de24	BUZZIN	\N	2025-11-14 18:45:11.193213	2025-11-14 18:45:11.193252
69	a9ef18e3-dc1a-4529-b704-354ec33c5d13	TONO 03	\N	2025-11-14 18:45:11.516096	2025-11-14 18:45:11.516137
70	04853966-6050-48f9-b93b-7854625cc714	TONO 04	\N	2025-11-14 18:45:11.763529	2025-11-14 18:45:11.763541
71	eba8a0a2-04cb-47fe-8844-65cc791620bf	TONO 06	\N	2025-11-14 18:45:12.289329	2025-11-14 18:45:12.289352
72	77b2366c-5a28-48e1-adac-9ebe4881d5da	130 BUFF BEIGE	\N	2025-11-14 18:45:12.582637	2025-11-14 18:45:12.582669
73	c02adcef-c1db-4f99-aac5-2ca44d80555e	222 TRUE BEIGE	\N	2025-11-14 18:45:12.794384	2025-11-14 18:45:12.794396
74	e7967441-70c7-459f-99c8-1b6f460e4116	128 WARM BEIGE	\N	2025-11-14 18:45:12.966925	2025-11-14 18:45:12.966966
75	77d558aa-bdd2-4088-81ea-a1b42424267d	220 NATURAL BEIGE	\N	2025-11-14 18:45:13.163423	2025-11-14 18:45:13.163463
76	38e809a5-9ca7-4ec5-ba3c-48fa91c2e828	112 NATURAL IVORY	\N	2025-11-14 18:45:13.367091	2025-11-14 18:45:13.367143
77	9264682b-a1ed-4720-b901-6dadd9217045	125 NUDE BEIGE	\N	2025-11-14 18:45:13.546799	2025-11-14 18:45:13.546838
78	ec6a5fd6-3932-4cc9-a312-5d81cff10e5b	118 LIGHT BEIGE	\N	2025-11-14 18:45:13.688713	2025-11-14 18:45:13.688737
79	2e9b64a7-414e-478f-864c-b0dc03066425	230 NATURAL BUFF	\N	2025-11-14 18:45:13.826943	2025-11-14 18:45:13.826963
80	7784aaa1-cc05-432f-9b9d-b3530c8939c9	104.5 NUDE BUFF	\N	2025-11-14 18:45:13.968294	2025-11-14 18:45:13.968323
81	5a351d11-f6d5-4800-843f-337266ed0a2a	103 NATURAL BUFF	\N	2025-11-14 18:45:14.172784	2025-11-14 18:45:14.172819
82	bb25492d-8dbb-404f-b6f2-da1ef1d80f81	106 SUN BEIGE	\N	2025-11-14 18:45:14.364597	2025-11-14 18:45:14.364633
83	7aedf475-7d58-4756-a0b0-e618a9cf968a	102.5 NATURAL ROSE	\N	2025-11-14 18:45:14.522287	2025-11-14 18:45:14.522323
84	9d754fc3-34e2-45c5-b49e-f17f870b15aa	101 CLASSIC IVORY	\N	2025-11-14 18:45:14.66934	2025-11-14 18:45:14.669384
85	d5bf4e08-9e10-4cd8-b818-0e848c38b591	104 GOLDEN BEIGE	\N	2025-11-14 18:45:14.830913	2025-11-14 18:45:14.830948
86	15902bff-4aed-4fb2-8d00-c0510a7f8986	105 NATURAL BEIGE	\N	2025-11-14 18:45:14.986041	2025-11-14 18:45:14.986071
87	04fef92a-c6cb-43bc-b016-307a1a42b10b	TONO 112	\N	2025-11-14 18:45:15.186522	2025-11-14 18:45:15.186551
88	7a4f31a2-cfe1-4058-af58-5b12a7c2007a	TONO 118	\N	2025-11-14 18:45:15.330558	2025-11-14 18:45:15.330609
89	5af99c19-7e7e-4d42-a80d-42a944409b24	TONO 220	\N	2025-11-14 18:45:15.475409	2025-11-14 18:45:15.475435
90	b3e7746f-9b94-42d9-b56e-fc7e7eedf0cb	TONO 129	\N	2025-11-14 18:45:15.63253	2025-11-14 18:45:15.632586
91	dc5c8226-51cd-4f45-a90f-8ffec41d99cf	TONO 120	\N	2025-11-14 18:45:15.783573	2025-11-14 18:45:15.783596
92	20cb3c6f-881b-4117-8779-2c62bdb7dfdb	TONO 125	\N	2025-11-14 18:45:15.93794	2025-11-14 18:45:15.93795
93	36df9d73-b4da-44b2-a792-75916d484b6e	TONO 350	\N	2025-11-14 18:45:16.182172	2025-11-14 18:45:16.182183
94	6970b4b9-e90d-4d61-ad91-1c1cccec3a6b	TONO 115	\N	2025-11-14 18:45:16.567749	2025-11-14 18:45:16.567781
95	c5b61249-69fa-49df-bc8b-495f912df3ee	TONO 470	\N	2025-11-14 18:45:16.904935	2025-11-14 18:45:16.904946
96	80e16939-4c58-423d-a10d-8daba83baafe	TONO 475	\N	2025-11-14 18:45:17.220476	2025-11-14 18:45:17.220488
97	993eaeb0-6310-4803-9d81-ad78b2dc1413	TONO 480	\N	2025-11-14 18:45:17.584721	2025-11-14 18:45:17.584754
98	2179b2b1-64c8-4fce-896b-5b8beeb90410	TONO 481	\N	2025-11-14 18:45:17.635453	2025-11-14 18:45:17.635494
99	c87ce1b4-0027-4905-91bc-809b58731604	TONO 482	\N	2025-11-14 18:45:17.669137	2025-11-14 18:45:17.669154
100	d02e1165-e868-4549-a6c7-8ee27b0126ed	TONO 455	\N	2025-11-14 18:45:17.694061	2025-11-14 18:45:17.694072
101	610afec4-845f-4ac8-8e11-a20b9009d1df	TONO 412	\N	2025-11-14 18:45:17.710702	2025-11-14 18:45:17.710713
102	d31583f0-a9dc-4292-8123-9baed509f89b	30 MEDIUM WARM	\N	2025-11-14 18:45:17.726432	2025-11-14 18:45:17.726444
103	62502aa6-6ba2-413d-8854-e6f32f3d4c7f	32 MEDIUM NEUTRAL	\N	2025-11-14 18:45:17.744087	2025-11-14 18:45:17.7441
104	d1533f3d-adc1-47e3-81b7-530ec889ecf4	12 FAIR WARM	\N	2025-11-14 18:45:17.759763	2025-11-14 18:45:17.759775
105	ea7bfce6-0e94-4ffd-8bfd-34fac7106086	TONO 4.5-5.5	\N	2025-11-14 18:45:17.778356	2025-11-14 18:45:17.778384
106	d1338a00-7551-4dcb-9da0-e7fa14c62874	TONO 322	\N	2025-11-14 18:45:17.792793	2025-11-14 18:45:17.792806
107	b1195016-0335-44b3-87b5-f2be7263be4e	35 NUDE BEIGE	\N	2025-11-14 18:45:17.807623	2025-11-14 18:45:17.807635
108	8a609f68-a072-450e-91a6-c80732d4f010	125 WARM	\N	2025-11-14 18:45:17.82704	2025-11-14 18:45:17.827053
109	b899e5b3-b9f1-4729-90a1-383eae2e113b	130 COOL	\N	2025-11-14 18:45:17.842992	2025-11-14 18:45:17.843023
110	5912dbf4-af67-472d-8854-609cb683d321	140 WARM	\N	2025-11-14 18:45:17.860733	2025-11-14 18:45:17.860745
111	735bebd6-49c5-465a-8358-5459644b27fa	100 WARM	\N	2025-11-14 18:45:17.876491	2025-11-14 18:45:17.876503
112	44fba4af-98a2-4250-9a9f-0665ee0541d1	180 COOL	\N	2025-11-14 18:45:17.892278	2025-11-14 18:45:17.892293
113	8dcb0580-22c0-4fd4-b181-84297d9d52c8	128 WARM NUDE	\N	2025-11-14 18:45:17.910133	2025-11-14 18:45:17.910144
114	3e3a404a-5a14-417a-9bb9-1c08b39660ca	115 IVORY	\N	2025-11-14 18:45:17.926838	2025-11-14 18:45:17.926849
115	f2662e83-b813-4f09-85c7-bc9116deabed	120 CLASSIC IVORY	\N	2025-11-14 18:45:17.955475	2025-11-14 18:45:17.955487
116	0f4cfe99-c337-4402-aaf3-649f35bdce0c	2 LIGHT NEUTRAL	\N	2025-11-14 18:45:17.971539	2025-11-14 18:45:17.971553
117	202e9551-d24f-434b-b87a-0bb92359dac0	390 EARLY TAN	\N	2025-11-14 18:45:17.987998	2025-11-14 18:45:17.98801
118	5e9137ad-2f92-48c4-839d-49891f7183eb	390 RICH	\N	2025-11-14 18:45:18.004221	2025-11-14 18:45:18.004231
119	4afe4c30-754c-404f-b8bf-b43116cf3955	MEDIUM 370 N	\N	2025-11-14 18:45:18.019959	2025-11-14 18:45:18.019986
120	5655dbd7-2075-47d8-8fae-f8f61958d97a	020 LIGHT-MEDIUM	\N	2025-11-14 18:45:18.036447	2025-11-14 18:45:18.036459
121	8b2e5336-07a0-4b3c-b952-d81dbe2088e9	010 FAIR-LIGHT	\N	2025-11-14 18:45:18.053433	2025-11-14 18:45:18.053446
122	f91f1ec6-5bce-43d5-b67f-67ea47bdcdc3	0.5 FAIR	\N	2025-11-14 18:45:18.069888	2025-11-14 18:45:18.069916
123	3066be40-c235-4eab-88c4-b1db53d40e6c	0 FAIR	\N	2025-11-14 18:45:18.086377	2025-11-14 18:45:18.086393
124	a0bd771b-eb32-4c21-8d30-54e24f52311d	3 LIGHT	\N	2025-11-14 18:45:18.101448	2025-11-14 18:45:18.101458
125	6ee43801-de3d-41a7-b0d6-759dff7f29b8	5 MEDIUM	\N	2025-11-14 18:45:18.117162	2025-11-14 18:45:18.117177
126	16c3593d-1f8b-41b2-ba92-922eddcd8c9f	1 FAIR	\N	2025-11-14 18:45:18.132888	2025-11-14 18:45:18.132899
127	8b65e00c-752d-4966-935a-aa7964ddfecb	2 FAIR LIGHT	\N	2025-11-14 18:45:18.147951	2025-11-14 18:45:18.147962
128	ddd0d759-da65-432a-8a63-a517ae7861eb	4 LIGHT NEUTRAL	\N	2025-11-14 18:45:18.162626	2025-11-14 18:45:18.162636
129	02f43d23-9bb9-4ee2-9ce5-d6e9ba26e635	8 MEDIUM NEUTRAL	\N	2025-11-14 18:45:18.177256	2025-11-14 18:45:18.177266
130	305cdf64-df36-4ec9-8b8b-326391bfed32	9 MEDIUM COOL	\N	2025-11-14 18:45:18.191226	2025-11-14 18:45:18.191236
131	b88ffe6b-9dd7-4ee8-b042-417fdea1cc27	5 LIGHT NEUTRAL	\N	2025-11-14 18:45:18.204859	2025-11-14 18:45:18.204869
132	845af6a7-e297-4e0e-8e8a-9ef63f1470da	6 LIGHT COOL	\N	2025-11-14 18:45:18.218961	2025-11-14 18:45:18.218971
133	28fc6bd5-837e-4f2a-9e15-099bb31886e3	7 MEDIUM WARM	\N	2025-11-14 18:45:18.233304	2025-11-14 18:45:18.233314
134	124d587c-609a-41f0-9b40-6010783d2e2d	03 CASHEW BUTTA	\N	2025-11-14 18:45:18.248077	2025-11-14 18:45:18.24809
135	49d2758a-adf4-4764-a865-5efdaaa2f944	04 ALMOND BUTTA	\N	2025-11-14 18:45:18.262776	2025-11-14 18:45:18.262785
136	736a309c-c56d-4fa8-87da-2961951e9dda	FAIR ROSE	\N	2025-11-14 18:45:18.279166	2025-11-14 18:45:18.279176
137	bb1b5172-cc57-4732-af9c-ed8f50dc4af4	FAIR WARM	\N	2025-11-14 18:45:18.293008	2025-11-14 18:45:18.293019
138	a0760423-c8fb-4442-9d1d-6ae59ce1ae2b	MEDIUM WARM	\N	2025-11-14 18:45:18.313183	2025-11-14 18:45:18.313195
139	b1fad357-60e8-4c24-821a-1b57f7843fd7	FAIR BEIGE	\N	2025-11-14 18:45:18.329975	2025-11-14 18:45:18.329986
140	3db06431-9cbc-480f-bf96-f5977e282f67	LIGHT PEACH	\N	2025-11-14 18:45:18.345626	2025-11-14 18:45:18.345638
141	1f6a8d31-4ee4-4afd-bd38-1d122d71d232	LIGHT SAND	\N	2025-11-14 18:45:18.361852	2025-11-14 18:45:18.361859
142	b6f5fe6d-136e-4726-bb16-6414c2dfeebf	MEDIUM NEUTRAL	\N	2025-11-14 18:45:18.37637	2025-11-14 18:45:18.376379
143	9725959c-bc75-42d7-8503-267c1a285129	DEEP OLIVE	\N	2025-11-14 18:45:18.391857	2025-11-14 18:45:18.391864
144	000c30a3-b808-4e0e-bf1f-e15e2ef83043	MEDIUM OLIVE	\N	2025-11-14 18:45:18.410686	2025-11-14 18:45:18.410704
145	ee7ea9d5-9108-4e8b-b8a6-3dd6c2f63170	TONO 30	\N	2025-11-14 18:45:18.429518	2025-11-14 18:45:18.429531
146	1d500046-5a07-48d1-b186-fb6e417b527b	TONO 20	\N	2025-11-14 18:45:18.448534	2025-11-14 18:45:18.448542
147	410790f5-0e8a-4f1e-94e2-f84850e713f3	TONO 15	\N	2025-11-14 18:45:18.4823	2025-11-14 18:45:18.482307
148	b68b8e66-ce20-4c03-b55d-96c98bdd89fc	TONO 25	\N	2025-11-14 18:45:18.499721	2025-11-14 18:45:18.499728
149	aaf1ea9a-ccbe-49a5-9bf0-9edd99ae4f11	TONO 22	\N	2025-11-14 18:45:18.515391	2025-11-14 18:45:18.515397
150	3c51fbac-820a-4c9d-a8b6-fef3f85308fb	TONO 18	\N	2025-11-14 18:45:18.529493	2025-11-14 18:45:18.5295
151	bbf5f095-7b58-4a7c-afe9-0f8b5408dfce	TONO 10	\N	2025-11-14 18:45:18.544278	2025-11-14 18:45:18.544285
152	2f2e0d47-6370-453f-ba3b-1854ab96972c	TONO 110	\N	2025-11-14 18:45:18.563412	2025-11-14 18:45:18.563436
153	5c0f9c9a-861c-4488-a06e-7dd9709b1fd3	TONO 130	\N	2025-11-14 18:45:18.581856	2025-11-14 18:45:18.581863
154	1a1274ae-cfcb-4469-a097-2b72e338abbd	TONO 144	\N	2025-11-14 18:45:18.595654	2025-11-14 18:45:18.59566
155	7bee9602-e89c-4e51-b094-520be1a2f5dc	TONO 160	\N	2025-11-14 18:45:18.611353	2025-11-14 18:45:18.611375
156	2dd9f5a7-4499-4d54-900f-03379b0c0a67	TONO 150	\N	2025-11-14 18:45:18.62887	2025-11-14 18:45:18.628877
157	777661e4-4275-47e4-9085-97e4e615402b	350 BISQUE	\N	2025-11-14 18:45:18.645594	2025-11-14 18:45:18.6456
158	0cf91562-f07f-4053-924f-133c55413fd8	365 CASHEW	\N	2025-11-14 18:45:18.662171	2025-11-14 18:45:18.662178
159	f1e47be1-58f0-467a-b20e-032cfd818a86	340 FAWN	\N	2025-11-14 18:45:18.678803	2025-11-14 18:45:18.67881
160	de86cc10-86eb-4a9b-8565-ccf058165504	330 IVORY	\N	2025-11-14 18:45:18.695922	2025-11-14 18:45:18.695929
161	69516f78-c1d6-422d-8c04-717b7cce3ee4	375 LATTE	\N	2025-11-14 18:45:18.712629	2025-11-14 18:45:18.712635
162	de7e5fdd-b4eb-42d9-9db6-3be505b16a65	370 BISCUIT	\N	2025-11-14 18:45:18.730005	2025-11-14 18:45:18.730012
163	6391e53d-2a2d-4064-a282-207d79a75500	320 PORCELAIN	\N	2025-11-14 18:45:18.747513	2025-11-14 18:45:18.74752
164	bdb11b86-fbd8-4ae9-bfb0-48d6168a9c0d	345 OATMEAL	\N	2025-11-14 18:45:18.762938	2025-11-14 18:45:18.762944
165	12b846c1-bf86-4bb2-9fc7-4950078a07bb	390 CEDAR	\N	2025-11-14 18:45:18.77778	2025-11-14 18:45:18.777786
166	8a96dc77-9b52-422c-a5c7-8540028f2384	140 THE WINE DOWN	\N	2025-11-14 18:45:18.794738	2025-11-14 18:45:18.794744
167	7a428cbc-a92b-4108-8124-3c4f0e66c6cd	150 CALL ME OLD FASHIONED	\N	2025-11-14 18:45:18.809767	2025-11-14 18:45:18.809773
168	d7d14dad-69ee-4355-96d8-df432c09333a	110 WISHKEY BUSSINES	\N	2025-11-14 18:45:18.824019	2025-11-14 18:45:18.824025
169	1db66814-1854-4d87-887d-50540f6d6545	120 ITS ALL ROSE	\N	2025-11-14 18:45:18.838184	2025-11-14 18:45:18.838191
170	28569724-b7a5-4ce4-a83f-1bd58edfaf7e	EVERYDAY SMOKY	\N	2025-11-14 18:45:18.856992	2025-11-14 18:45:18.856999
171	d2b15ea9-164d-4591-8be9-e0c6a36d43de	NUDE ROSE GLOD	\N	2025-11-14 18:45:18.870858	2025-11-14 18:45:18.870865
172	5d3f7eea-0095-4594-b1ce-8741f735f8f5	5 MEDIUM MOYEN	\N	2025-11-14 18:45:18.888167	2025-11-14 18:45:18.888174
173	24678e1b-14f0-4505-9419-9f1d5bc1acab	3 LIGHT CLAIR	\N	2025-11-14 18:45:18.900955	2025-11-14 18:45:18.900962
174	1bb308b1-720d-4c94-a0f7-814608cd07c9	4 LIGHT MEDIUM	\N	2025-11-14 18:45:18.915299	2025-11-14 18:45:18.915304
175	25c7a7ae-2bb8-45fa-ac55-d36672b33472	PEEL 2 	\N	2025-11-14 18:45:18.931979	2025-11-14 18:45:18.931985
176	4a09b796-5de5-49f7-b8c4-536e063e24f1	6810 MATTE SCULPTING	\N	2025-11-14 18:45:18.948297	2025-11-14 18:45:18.948303
177	e055685b-6cbc-44dc-8c95-9a015fbf008c	BRONZER 2	\N	2025-11-14 18:45:18.965225	2025-11-14 18:45:18.965231
178	eb8f4b80-f776-488d-a1f3-bf5358e5ce15	319B BLOSSOM GLOW	\N	2025-11-14 18:45:18.981913	2025-11-14 18:45:18.981919
179	9cd09aeb-7dc4-4178-9a56-2f057db42d15	FOREVER SUN KISSED	\N	2025-11-14 18:45:18.994875	2025-11-14 18:45:18.99488
180	249451e6-1bcc-4bd3-a6d1-fcee0f3b9f34	TAN OCLOCK	\N	2025-11-14 18:45:19.009203	2025-11-14 18:45:19.009209
181	d4efecc6-9718-4d66-b82d-badbafc77c93	400 SO GLITY	\N	2025-11-14 18:45:19.022675	2025-11-14 18:45:19.022681
182	28f7e2be-b23f-4ece-bbf9-1a034e42f757	LIGHT 1	\N	2025-11-14 18:45:19.038259	2025-11-14 18:45:19.038269
183	dc60ea0d-07f1-4b08-a75c-f8f2a8f43a82	15 MEDIUM DEEP	\N	2025-11-14 18:45:19.057205	2025-11-14 18:45:19.057211
184	75d9eb10-91df-4931-a499-77ab959180b5	610 GLASSY PEARL	\N	2025-11-14 18:45:19.07091	2025-11-14 18:45:19.070918
185	f361be64-df31-45f4-95df-9978d96024a5	620 GLASSY PINK	\N	2025-11-14 18:45:19.086871	2025-11-14 18:45:19.086879
186	6cfe67bb-67f0-4944-b611-20750071c4ee	125 IVORY BUFF	\N	2025-11-14 18:45:19.106548	2025-11-14 18:45:19.106557
187	1959d225-b6e2-45d8-9349-c80ab9756fc6	5 PEARL	\N	2025-11-14 18:45:19.122033	2025-11-14 18:45:19.122039
188	0722cc91-fec8-4fb2-a2fa-9f4c5fbcaf68	180 LINEN	\N	2025-11-14 18:45:19.137016	2025-11-14 18:45:19.137023
189	ac838726-b3d3-4bf1-ab4b-44c8636e3307	MEDIUM 	\N	2025-11-14 18:45:19.150571	2025-11-14 18:45:19.150578
190	639c5831-34f9-4453-b9d5-751118f8f507	LIGHT PINK	\N	2025-11-14 18:45:19.164146	2025-11-14 18:45:19.164152
191	356a37ab-5d39-4b6c-ba14-9dfca10962f3	40 TAN	\N	2025-11-14 18:45:19.177248	2025-11-14 18:45:19.177253
192	efc499ec-910c-4fc4-b433-061163901258	EXTRA COVERAGE	\N	2025-11-14 18:45:19.192267	2025-11-14 18:45:19.192273
193	d21287a3-3c79-434e-8555-87b9707a6041	245 CLASSIC BEIGE	\N	2025-11-14 18:45:19.227572	2025-11-14 18:45:19.227578
194	141f58e0-60cc-4316-9007-ebf749080078	100 TRANSLUCENT	\N	2025-11-14 18:45:19.242084	2025-11-14 18:45:19.24209
195	ced2d14d-f9fa-4d25-b1fa-6ddeb927728e	200 LIGHT MEDIUM	\N	2025-11-14 18:45:19.255639	2025-11-14 18:45:19.255644
196	6a7856cf-4c29-4bc3-8078-0f2cb76b96aa	PRIMER	\N	2025-11-14 18:45:19.2723	2025-11-14 18:45:19.272305
197	645913cf-881f-467e-9e50-e47279ab09b6	08 TEA ROSE	\N	2025-11-14 18:45:19.303933	2025-11-14 18:45:19.303937
198	416ee7a4-1211-45f4-9f28-55a2babcdf27	02 ROSE D ORO	\N	2025-11-14 18:45:19.316528	2025-11-14 18:45:19.316533
199	669c4c9d-536b-4ca8-af34-0eea18e50d5f	12 BELLA BELLINI	\N	2025-11-14 18:45:19.330683	2025-11-14 18:45:19.33069
200	375c070c-da34-490e-99c9-6ef294d50d00	01 DOLCE PINK	\N	2025-11-14 18:45:19.344433	2025-11-14 18:45:19.344438
201	f16111f0-cce8-404c-a6ea-560b225f43fe	14 PETAL PRIMAVERA	\N	2025-11-14 18:45:19.358261	2025-11-14 18:45:19.358266
202	7c5febb1-7a38-4d13-9c71-b762cd4d1e7a	BERRY RADIANT	\N	2025-11-14 18:45:19.371499	2025-11-14 18:45:19.371505
203	9aa0760d-4dd9-4052-b282-abda27747cbf	YOU GO COCOA	\N	2025-11-14 18:45:19.385117	2025-11-14 18:45:19.385123
204	f4c162cc-6700-46ba-9048-f21763970add	BOLD-FACED LILAC	\N	2025-11-14 18:45:19.400551	2025-11-14 18:45:19.400557
205	ab8534e3-502b-4aad-92c8-bb92bd173b87	CORAL CRUSH	\N	2025-11-14 18:45:19.416601	2025-11-14 18:45:19.416608
206	fbfe73d4-bb1d-45a8-8c7a-1b32a296fa87	BRONZE BOMBSHELL	\N	2025-11-14 18:45:19.432815	2025-11-14 18:45:19.43282
207	34d7c802-3150-4e22-9bcf-7516349ed688	BERRY WELL	\N	2025-11-14 18:45:19.447171	2025-11-14 18:45:19.447176
208	d67050bb-2457-4a27-ac43-79ab177cd765	SUAVE MAUVE	\N	2025-11-14 18:45:19.462506	2025-11-14 18:45:19.462512
209	cfca9a78-d53f-42e5-abf6-5841d4eee58f	COMIN IN HOT PIINK	\N	2025-11-14 18:45:19.478403	2025-11-14 18:45:19.47841
210	171ed602-959b-4487-8bb4-d6129dac52d8	PEACH PERFECT	\N	2025-11-14 18:45:19.494219	2025-11-14 18:45:19.494225
211	320dfa78-b082-4a81-b9de-a387a3cf2e1c	04 U KNOW BUTTA	\N	2025-11-14 18:45:19.51083	2025-11-14 18:45:19.510838
212	c7803985-bf42-490d-b295-d6c1fc994e2a	07 BUTTA WITH TIME	\N	2025-11-14 18:45:19.526684	2025-11-14 18:45:19.526691
213	102376cd-d560-426d-8184-425938ca7575	08 GETTING BUTTA	\N	2025-11-14 18:45:19.541541	2025-11-14 18:45:19.541547
214	3a879bbe-ecc3-48ba-833c-e049360a3e2a	DAZZLING PEONY	\N	2025-11-14 18:45:19.555392	2025-11-14 18:45:19.555398
215	fd5687ee-cfaf-465a-bd42-ed2debc40586	GLISTENING PEACH	\N	2025-11-14 18:45:19.569139	2025-11-14 18:45:19.569145
216	c69985b3-22f2-45cc-a0ef-e2733ed4f6a9	SPARKLING ROSE	\N	2025-11-14 18:45:19.582956	2025-11-14 18:45:19.58296
217	9c2d745e-5297-44c6-be93-daefb54e9254	TAHITI	\N	2025-11-14 18:45:19.597487	2025-11-14 18:45:19.597492
218	fce19a85-3e12-4e93-9df4-a22329080f18	ISLA DEL SOL	\N	2025-11-14 18:45:19.61295	2025-11-14 18:45:19.612956
219	8949b4a7-9051-4a95-89fa-ed817fc3c9e7	SIN DEFINICION	\N	2026-02-03 21:51:56.449629	2026-02-03 21:51:56.449654
220	8f677503-b920-4a3b-a480-d45e5bd6fc8f	TALLA L	\N	2026-02-06 21:41:48.095943	2026-02-06 21:41:48.095976
\.


--
-- Data for Name: digital_certs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.digital_certs (digital_cert_id, digital_cert_uuid, digital_cert_name, digital_certificate, digital_cert_password, digital_cert_status, digital_cert_expiration_date, date_created, date_updated, enterprise_id) FROM stdin;
4	3b903017-8a86-4836-bf63-d1630e90d4d9	QUINCHE MORAN LUIS ANDRES	\\x308223bb0201033082237406092a864886f70d010701a0822365048223613082235d3082059106092a864886f70d010701a08205820482057e3082057a30820576060b2a864886f70d010c0a0102a08204fb308204f73029060a2a864886f70d010c0103301b04147d9bf4fbdb33892c5399668d01933334869646f3020300c800048204c8cb13409551a03f2c0939b2bdff436861030f7de67709f8a49cfe1f8f1aab4bab02360cd3430dc891f9f6d3c804f703eea46693619e1c104e71bfd0276e2a3ccf513af0e69ea7617fb3ec2a6eebf70722cbbdc91daa68be49c0dc36636888481535622092815888b5e5b39dd2184480b7558b35faffda226953cc38195d9160d2a5a7e8ee154b6d0fc1dfbe0f4759202a73ea6118d847909bb0df9469f7b215890421b3bf673c4e3b68694abb841144cc316cda4b1ef14f4b87825df0ad3b7eb4749b6bb043cd77fcda4567f6f0c1bb03cc96ced5fbb96ae68696c81677d9787682f9ee02badfba615d9a733ef2178152c5f22ad68d3b7e6776bcca8e4e74d8c4f7d7e2f744a1e081c2611a0305f8a51251b652d3f4cb383174a22b7a40757a2036df3c52a68a4a4a2e05f64cb932ce12d0a8590a7a7b2ef445a44699c5eda645fffbd3e39b4a11f17aeeea54f78fcc982696424e77f3beb84fe07121445844a60dfd17982c53acd61a2259a8cf0c01218dc08c7fc1247e5d967f3932602bb83aa236410307710326c7f0a29d02cf0b6d841e1a08474273c2eb8e94399d80a6c9b4a94468697a93c165bb356ec6054fc985bf070e6502cfbbbdd255996cda4bf33f604d29f3bec148686fccb69f2f450b537634c2c26e412331b4cb00ddd1fa6b10125924efee206fdc849764e8e8631c35a9833b8179566e981fb7cef661ab643d5258fa41a3b0a3231da151f3a5da709f6bef0a44b1cee8c41ed0c30080bb69a8276341cc0c2c22d627cba1dc094b3e3ceaff0597c4f44435e42eb9c2675bc0bbd011eebbd84eca45d77546b8e1fb114fed4572c75bf23ee0c196024bd8a568d03a6468ec58e8ae915a5509fb58e9cc17663e2670f87ed03ef0643032ef90a06c2c41bc2e99a173f1f77fec023529995c47f03da2324e9ca832aecc2f86e33847a7057255a87a3f851bed79585c81371c5ec65f841de826698543c6048a066da2c78866f3472e70fd70107e70fd7fd9aa796a89741161cfe733c11d59aa7a1c8cbd0183640c7f10a5e194e1a8608dcdbba3c50b58657b0e9e807c0fc6f5d3d264f796384ee5c32b6a817faac1f4b6154fea30e84f3d1e6488ff4a39bbdad9bd7a47647385114424683c39a92d708ca215f7bf924b9702cb5619a65256701b0192f45e415f1e4d1c90dafab57fbd5758b32deadef3243c550c14d61041aa38d24b6622de48341d5a8f18f9897db1bf1c6edafb23192b8c4465f8416ba7637c2364d400d4a2cea09eee8c7c48f11bc6c0453c692967845999caad36c38664b7caa7d09c3c0062d3541160198b66c8ee28ae8abc6a68832a7814564221ab97209453d3cb0bf71e45b17e45847276b6cbe17bfc9a4d085f14085b8d61016671d2a6b87d27efffcdd10472d308ab2acebbe72025e61b4c39454bedbdb3a060df8f9fa4b781eb575a341f3070dc592967d4b6daa61e4fe4a09848564d10dc4adc0bc9a16c1c8c840b636bbdd7abf4264a859e4bbca59099d32eee607759492de565ff7ee1f94393535ba1516975f8a59e3f85e0678ee9d33069546f33163738bb2bbfe65291021d83abd624143c15a1d7753e03d428bbcc8fe6efc1ecd3e86b7006d8a62cf99576fe89957325a16aca8687d885f2fa3dfc563f7f7b2949bbb8fc894ad80a6a135687c71f1478fcd67a931d3811835776153e834ae2199ad482d8188264b6306bba3129613168304106092a864886f70d01091431341e32006c00750069007300200061006e00640072006500730020007100750069006e0063006800650020006d006f00720061006e302306092a864886f70d01091531160414c5cf923485f9bddecabe417d0c7787f49db6594430821dc406092a864886f70d010706a0821db530821db102010030821daa06092a864886f70d0107013029060a2a864886f70d010c0106301b0414f896e3f71f26ca73f80ca72d98dabf79b985f628020300c35080821d70839ba0a872e6e65c512ee8d73b765f69f2e5c3dba2132ef2dc75efde59e3ceb54f4e7caa1b946d15f090b1f03cbfa81713f065e3b03da8cbb708c57daf51bd7def5ec2755447042f34f37379b905c73e76f33cd0d2c08e7a4cb157438e7279daf192a1ffc70645abc37f53565b7f5af7dd7b57bd915ac7abc70d362e60139a95f75c3bb0308974c9d4f5cac45eb722f89ab9e2d15aa2fed1b662c3177d313595ff08616e3bdd5940e5011f5b9a3ff44010499287b6e3880da16da010979dd2cec2b5aa7ad3131c3a097115dc11139b6443020c16473b677e2168f5ec8e9969e469d1ceb994870c56b80570b678dc46aa6d9ad62c8734c64e3fce2c8198cdc2be5c2f16c1b8fc31e6e98d0e09f645679a040945536a9fca7b8d3cd7e00b2b3208cff8007a9046f619d3c51477c726a2213ed34caf1137249fc491a4e40349674c46e818f6bb87e2a053534b2cf66f46bc4b490439aed631d72723ad026ced8917fbda72b2f2cef4b4b10a5976834368c20ef148eae930083c00e1ac11791bc064664436070b797ff74c24c66cdbe740c0d1e24ee24592bb29e120fa3229e6f41f07d78e12a98c2f9adba4605fdcf42658a311d4ab0f9a07cc32e7c115788673a8037a50b7546d1e580be5120ef9e22361c86741071cd30e8e63d33d4928ed4efe9ba8353f4ce87cfd7bac326ecabe36b5360a6e998a7464c1416351ba4b86091ebb0e3d3406a9bf2111dfd7df4e3b6338e192767a1ae00e9404c0acfc132eda6cbc2a88facaf911d4c67e6a5e9630fd838e7ad7f59c3c1bea5150e5dfe589c848c9d86b5f4b881552148104d2ad512a92712cf23d8cff4e06ffdab32485eba4b5548896798670464aeb387299a07d1a470400536e338dc8e9adc545454151427825064452616b34e8e68afb99dbd6db4265aa1bfee01bca2a053ce2ea9ec6d9e0e93b23dccd09fba1493ccb94c0bf4edd7e82292568ab1679c5548c8e3811e15a6de32f41929c25d1b84925c46192c16a8f0960c138ce836ae93244f25d760530c5eced929f218a50d66050b17758d6f0f5f4bea2874d5195d7962f248d372dea826e2a5747c634556d977f000b02836da78b2cb296acbd446599641c4279dc665af51fa514f5afad04b2ebaa4029eb1ed52df1d99b846f3cdbc06708eb0f7362da6059a831167644c33c8e02500d87850e465cff6bad1bb593147b10ee291654518016a0abe9b77cb5ef1841d6b258e3f48d228aa444d261bff2d0a2a6761972fefe3245d40e2a8ddbca0ee2a70ad121368f3076e7ad30f0cddb1d8f0fd94493c32c0793d823bf00113fe9f475623821145b58bf9c269387af41519d00f81454e4bbf1cdc4ee983d5e00319b309975504404acf941d01525a4a5cbebb1214c8c2848f2c9454d891eab1e2ec4c116e40e88aff59352b2637699c109e4725cb4fe1825c61ff07f0428e3e4576e6fcc84d9cfce39a7ae787271a9488bce6571408af2edeb0214cf8fcadc6e3d901b23d8959fc4f0d3ceaf597b30ffc6b41032b09f3bbe0deac7c560f4313ddc385c6ac07b1c38726d10a6365dd444127e1ebfd6fe7bac65d70f680854633ff66d11a5c05800749a0d1672b1fa88722f31aeaa5b44c31d976fa9fe3d0fc6d668e17805762b6e16f281d95fbd6809d59f70eea03e1ee192e5910f22b12d4ac6655b98f7d54de7c995d618c499ffcfc2b8ecbc8429cea19621242d9024de02082238f72cc23fcf5f9ee2ac1c98b5bfe14d620efe7df0edfffa64fe9cffc2d9c4e63020c5bbcef9a3dda06598d6be5ce972de7229af8050661dbde321845078baedcd990113a1643c50ed27266445e4b3d9b0b78cc5be66baa47869de0f713b2c5c39e5e37d3c09fe002d6dd2d8895317c2ba793fef260514cf5563ac3289eb2cf9f72c700a09180f33528e7996346483f22533668e447ba56879826c9be67c571c84e8ceb238e0b85bb2284d720432fda5c677c79a29c6ea6a74a98e8df9c8728d7a7324e2f4438ac71d87c65b7450925b18ed3ab8471ea51723bd5deb86c5b575537721df01ecf308c47a0273a77475e1ce3d00a68624cd000a48ad3602cbb0155e0f8772976f0313271de2ce0c9a28cf78681712193aa42e21928e5e95f667aaa000b67514ab50ad0797309b42b2c09aec98d0edc1bffeea2d351a194880949fb4b0a0e29d223d3f666c64c3cfd7c91d64b62243b974a5471933633a96f9e50fb9783382d5ccc03771d2711d189270daabfa4adc5ce03f55bc4fcbc3ddc7a3408d657b36604bdcfc79e4a6996d5c1ff448e7b3594d1881ce3be4eb0c113f3482a7fab13ac8f6208529be55307bf0483709014f0c36739e63217f37027839f84ad7888d19da29d4bfb879ddcd7afe99efca2ef52900248caf9faf37b9b4ac420e5569024eed56a04efd6f612dae9550ad0734aa3ea5eba4d00e16ab595a57c1d02f19860550f198b719b2883e2b922dd388e70f99f0f6c338bd21e22934b1c3c8420608d677b00d07fe914f304ff0610b828af621e6cbce9ededeac29fb141914ba7d63cabfe1a867e63547ff923106585279a6674744608a864b6d219bf2f7104e47fac63d5f606fb8d2e063d154cb0dc8e5a2cf9727e4d64c9713782f7f8ba304f21f94d856a09548b45abceaf5be0b7bf330ffcc7bbd2cac08511afdf6548763831c5bfc5211a251d0f0fda6bbaf7cad81e51c0745dce52ebadc1db1f3a2c6f1d34313a2a222ccd817d8068ca188f20121032bd87fedf8148222f82071aa6dfe8b2a4ac05529cbdfc1d9bd48d3963fa1550b458fe850c5ec00b5da1ca4577a1a1986a56e05c14a9865b48d0570f21ae0a33cc27715eb31cded18bbbfd3a76bd190b020c06830a616967b02e19a6554b57167c2bfe2bc87939aece686966f403d3c21c1539de13edfe25d24b7283765af400f598f2bdf724c80791901af6960238382166807179694af43d1fb8247682cebd100416c8c44e84de63de82f87e39b2bb96942530292229708d31e1608cb9cf5339c6ec182b7e552337d59e51e3bd02be41211ed5a7387056bf42e55ad32f8d7b9bfbc53bdf78a2cf06ad9d59ecf3eedf3c745950b1527eefb51e6ed017ed5d5845d28ecd760df7dbefe0aa15e818479116dfb0956dba549fb3be06a8e4cd1818afb4484a0659f83c05ec03ce8514c67c6ce274a5680af978a3bde52bbb3428d256022a366e7377db24c44555cfc0ecdd0b75760874054935b7330bb561277d297c384cac4d42928ed42466e40cd1f5783cd734be3876b9d5de3f6af5943ddbd245be8d9a57925e37e8271a03b19f5f1509806a0620744f918d42fbdc795075f2ecd206af6012b039cd515b6f1daa0b3101c803170505f0b15228e05120041ecfacc78cb5fe385681139dc87858a544b2280aa3ede40f9a36374f9e4881eb671ec02224bb5add3c8a1b088f014a05af27ff4ccb10873dce993db7e9ab498a407566da04917dbc15104c65d7bd3dc10c9582579d72121bb765b2f4c7346a9d0505823a64c793ca40e3345b07d1cdd030dddebf0a2664e893c67215b53ee7737a8348603a65123508af6fce9cadaa3aacdf20434033cbcd74ad87230db149c3472548af12d23285180e06dbf4b45976715291d16879be93b229c0bd7e2dbbb6bd6f5d3246d57679f322a33584ba78a3c329a65d7f074a967dfc068d7e3a690a6314ce58dd28e82850e571826f516bad08a222ce86463a6f131d9dff8601a19b172c0a4a32f99bc6b09aff330661e3a38fb7dc09c0a54f16d1f45db4e88371fc8956e0720ad38595e21dc212322c97480f44c012fb9f55184aea798e0516ee874fd05b73b932d5a96d9f4dfc3c53d71ada261cde5480cd558a450758ae749a688353bb360d9a7cfce16ad464d36c199de36e9fea0509a6fd716747f1c3bbf0c955b4dade292826fd41b443100bed566686593d8b48b760a5889fb600c7d77579dce89d7e8c9596d588d1b4d7a5ada4296acf8d2bd9e68f84c46ade0459334b61d3826bef4f18bd359a37ab5fb3643022196d47b4d84287e58458ea8556d6a4b64e49264d3f365c497d60e38040d1f4ceff43b5e4cbe2c9d4a5780b6f09ecf88db8c2d94a3959ed5dfbd7bb10eb61d5df711cfa7425d043245b3f6aaf26a78128d4200d92575d08685db0ef6463f41c601340110978e74e2a2260a13281539909423becb5ccb2385a90755664293aee37f5e1a712f77d25faa69de994f7b3fca7c1c891e254653cee4d166f9ed9e5c2f9b5acae942b7bcae4b38487b1de6beccc68f496e885a206846954893f73417bf1f014e6bf806716934e94ceb4ac97f0fdf80c69e5ac0e80f157c1c54b64ae4380c047f5f27b694574d5c96eaca32699faea3d7073c87988468f2a1209b8a284c46cc05acd134fc757551ced854d81db8fd2c7cc41775a675058a89a54b6cf47a014032e64afa5c76f5f1dc8c76c3be6697a380b696e5a97a9c14de5d360816251412a3f86aaf80087071475a8f73f0b79f74dc4c4aaaae4dc8f200265f41fe184207cf63e115073e9e50028ec3c45d7e44eb1736f1c34ebd6d062480e2f3d0e8e21b9df05bd6e029e084d1dfb7e58084188366a81da18e65e8241ce4009aecf8ca8f78311e4322f6fd24b71dd15c407c8a6670af72e03069bbd79a0c3b07a4cd5578e0ce1979c86bb7f7ace1dd5ba305050acf282ba9083ace3c39dda7d5327ebf488c0bf56058e93deb7a5d5e4cf230fddc89c3f8ba5648ccffa8b4455a12413aea8f6dec8e06b44774a2430876b0059e63c7ac132155812827b34d5663046d6123e17f47e66af2b1fde1e1d2d58898b67c2d410755c732eaaab3d54d33f61ee4e5c41a5afcd8dc3cbf849473f12ba73b14dc58cc01dfafce79e387cfe0b13e12712cc62741fbbc61627f1b8f07a8e599e419751e413830f1892462258069d5da953e3ab0fea7f59aec30cf24148dce687c14bba09d573c7f70b13c091cbe548b6f22b459d732ecaabcf110af9e28d176673c528efea3145507a57560e70f697e0c297514c957f22543687b72026feda5a28f1a1c5ef274e20416c6df2971612f7053112f26b978f4097f8f4c921e84dcf8cae2557f0ddf022b52014787bfb09ca302cb8059af628b35acecd29c10b850d51a7158152d97619cf212057ae3990dfe7a7114258403b937b94104fbbeb5de4ee626701df07924da2ac08986d007b1227168a7663200f05610bbb7d18190bccd8868f41f5879521ea5887c7c4fe023f707d02c18877411a49d5cb7042dcd898a4e0893b058ceffea3612fbb29ff8e963f9d2e23ede62e67f0ba5d984d1c45f0b6a85c83f146c1747d08bbb3c1811fc2d26da7b91b954435dc0e3f8b9cca01aa1e49a2adb8c2a030266e5a9905970f39e63fa3103d0ee6b9a8b105dfc52785c53da821ee14825994ade6c2992000b648f1a12dd6cb614dc4173f5f0104bb8ca38dd07684fdef6c5395ef717b279daf38b2818b86da298eb5958ef6c25894ef992e23fe2429c66a53fa314216bd1096e3f59779fd19dc4e7ed0ad118dc1e517f4d26bd7ce1d684d6cb7a987f2dcc6a1a984faa9f70f55edf5888c0951525700ada004950f1c779aa30f924f2fbb78bcba9244daa2bdec4fd7a0f65e84c6d910f335c41c40f500bce22937bd9b00d27daf50594a6f454da3649d8fd80c4914935a603742d704dda522b2de3583708cb226d1936205509f13cf2c3b7caf34d042a94caabc0108d8bc701b22ca6ff315ec9366866cef208848e9690d9cc4fbc6fd0190a7f2718fd1c9ee7919d39b53d9b75b60bb0c3864b5086ec1da89ccfa09d5845716273c0721901554fa83277ff7d561d52b5c05f25c1afda8624c8ca2a708ccd8f2631567c1e406ecf282dcc6b2c1fc5c6d80af695bea3cc23d6ef6e80a2ba5bbc61a1f9c6f883600747d7edae386cc420a0aa7f6d81d8344ce30311c47f48bed7387e53e743060804e4631292f3e041f390c7b892b9c87b3f40fd6cb8f0126518ea7c806601717b01f8c8def5df774b04d0b0f4ce91ce0d0e151b37c05bd645fe69cb936e7e8147df21cc1cc90b9c7529e262c7619592db4864b9322f6b087b1d0859ef106f0bc71b2a25ead714b1425630e47a2b5e1d89d56594833f3d1f76dc6ec710bea5f612b9fcc08fb054e81131389817609f35e5c2d6ca3bd9b465e181287eadb3f58f85eec94cc419e8791740ecfe349299ef7af73a3a30fca034593ba6ab690a416f3d9822266eeb5bb9cf8419d395bdbc346e3439cb72bd3b9c9fbb12c9f64739ed5c325c371c065f4000c47f5365ffe48d565813482ffebc8731521c9be7f80a87db6e82855e5f8c44f7aa45067f2f6bc0c3890456fe99892a946d30d92b11338fd442f3402f1c38439f7036c2d4b22e92be059d6b677eaf97326b69c92b41939f34981c9b8b145dc764d9fbb32c83a375e2673532ca9a49b862525065e4a97b37e5f0971775d29b7be99a0a7d372a20daabe0314826a1317f5f370461eb5d2453a74b3a4622dda77aa883564d6a23a8d2b82c2f9b9a895e8fad7d94ee708c57fe79dbbf88868d94ee7dece5c9853987db184bb0e1caad331dc62dbd0485680e8c5a87ede279117323189d689debf9c95c149f727ab6efe0c253a15a9ecd5554804439ea91219ca506d508884d972cf4b89ce8c3e8e46d713d45228bc8972030dbab89366a348f8099987a42bc064c44980b6abf70bdc43a96732a574363108f2b61d94df3d4efef019697b482838759038bc32ce4bde7be9b00dc16edfc3c11ce6b89449a80f688975ae958e3ee3a4b78551601a77fcd0b0135f34d9fa0a4e906234491188757b3fec139b09b28d9c682d3ff19499864d9ec9c80cefd229edf51c15d42a154e040cced16a2b22b6d220761374c71b2c4a52fe71afcde6b53047c2f28179a0697967c21380b85482a5f0c7cbbb346783a97c1d9bc12f92ce48e06ae219706e295496fb9137a490f5bedd07003978b443ca8fa5f6c747176c6a79a0026ab43f155feaddcc180e80ab5452fed84ff3b40fad86bffb98d52e31768e4047ddd514dc3717c9f5c13f0c334a4e3a006201e2bd173bf0c6a0aa73d5317c35f84319a9da45478e50d5faac9c4dbea5814fe1d9b91c19364785a028eae095e93e413a0384116bf61bdb0bd1eacc3e611d6942553d2e79f2fc2dd4bc5c8b31b403da0bdfc822108bee2c4ce3f98e47faa6647781b75f69f170ec829bdc19e33f84ee4e472be36a6c55e0b2fddc3d3ab0d299cadea5574e2ba1745805f2d2b9cf7ab27168d169555b7d4e8e1d65424a391139f49d98c8879a516502b0187d0a3f81485da1997828dda8ae5b6af888f38e9a3dc2715dae767cd75690d745311a723ad343ab4d4c1b0f8030dca64bf96ce80a74297efc44bab8b140a2d2a47edec52f465986424a176de43b89e38eed833e7d44a458f6cb86147c0f5a6b30ab45d856952746a066d25fbc9224d57173f83ab5905f976b0047a54d82f53aa50e38bf218f146391b3d3350b337f98db2e87a7e6b8f302ecf2de2021a23392b2122b9dccc21c0ffdcaa9e9478682c94b86db13fb28c745dec0825d3afff2e8c98cee4136369d350d7f3c77cce7479398024b55fb1fc9467dd13390071325da113fff3a6e670aeb14eb73c8c04883bc755b4f1f108d76394d14b4eb79ed285f2e34060c57c68668e94bf48192aba8bc0d7df5d50c34c8a497a4c105f61322fa73d5b7523a31dd51a11edbb9499dcee5e05edb6441bc3bf89a1697baa010dc26a7300b793b7b128a0580a4d05cd32500ea23e586cddff234c5a9b2330901692dd1ac8871f237d0930e7ddb71d356b85e55695d4fb3c5a505c5ce70dd31e872b4b3edf17e29363bfa383e20f37f0a35033a93243f650b4b1639a12fc88532eda93204bff02625aa8297e5bd5dd0f3dd35b03f6b0336c4b65afaff8cde6b5fa13ac26575a4c9ded93fa507c09c66793bc22ba70f4cfbb0c461388d587263dbdcf5206e4dd9809f5e6c486bb03944cc62b428519426539393c3b2c299c6f95151d63512d9adb994d86e421539c8ca4fdf6f8f0c5281707b5cf12af518b225f8cf7fdf87ef7c368bd8693a59ae31848c3fa909a628447d592eee7a44d7d7f562c1342b34fafc1ba72ec85e444881a84b59987a039d6b5e2b4ca24d6687c9a786d639f9c30294b48edaea88fb510c1587927a2b08c45f2c9aabae2b8185483d8a54f009007e8401c7e62f099ae073ce84ec538033f571b7b6889b436186d176b92aeb832816293c42b3352ab01049a031a90f8ecbc4927bb0341ffb9694c8c475865ff65668d1b1af494bca46d3bfc0619a6b020c869b3cbdcc5c949e175088b68bc5bf1961c07703c84ec2c5e3762c600a1e5a617ed8e4cc5d2e7578f363d589c9a4966bb59bdb527b2bf24ad7c9ef774e3cb5dfcd250b7e4932471f8d606b1ba54c07842e30c2c19068d1af43e2f8d0cf86af0ce86e5bef16c597b3ea65abc6047c99fbe11b30fe2fbb2e56fe67c17a53f1541ab713a948fbbaa438f5bd6942a1bb7e1e7d1f36310d137010dac8702f8727d8f81c5ef65a6156f6fce6d6e6d96d7f225a3887efdb89de7f280aed11dee6af3a8536fa0121a7863ea6dba42c04bd1c23e62362213c3c335f075a586812d4579478ae4a95c176aa338df612aa977f52bad6c4a06ab1f1ae2622c3a2777a05dd34bdde227350023cfb05e4536f28209c99bd1d232881b9e789fd7c9fda36e6c22299a3f3ca1b6366fbd3ecdf41aa12f3ac6e32ba9314aeb051661dae798b0f2de409f952bc792894f17b3bd8837ad8d64b8e9fcc5216aee52512c858b5b739a1f25e5878b6011ae4dc816d850643b2b5ee04048c68a40f1598de0b9b43d9b5cab047ae3ee147602e65f783bfc108c35e93ab0c9ddd43c8338984c6a00a3d816002ffd1c9bfd6f92a569eaa1c667ed64006c4faaaa7b9fc15845ac0275ab3e014eb4e1c2987685a1f23d993dda772c57b40d2d9970ead8688843214f1dd01c2eb3609accd8e5885681236f059c59123431be456ca07fe0ea843f5c4eab2b200126b4d462883e3a1809f588fcabad16d1d92208824c0d2e1ee0a3477664d97746f0cf88c6ca37d5b6b6b0dc2dd0332bf149c423585ee178e6919621876722de840e60afa43d30c7e7c54b58b251658b1b620795172bde19379e573066041b6d8ac6c822194aa5b59b95503f98ef81d140a3c664a20d500ddbf6ac753269ad4900f50dd7ea1146b5c694314f685dace621660b70d5d00fc3cae913cdc5c227fa776a3db55c791e1ca7084772cba0b41c9f23852914c46656afd98b8056195bf8866a69973908d57b4a3f11f814e6e457b137142e50d96cb3c2ffdb6c9d9d1f0e572a9c7c58a866b7e432649a709e05c2824b778f88d96dbcfc5efc9381f79a3d1b6a52f5bcd5c5143660573cc781a4f5161bcb3520b83351f5157905ca18f6364373040269189ecb343386e72adbd4e6107b31605df74758b08b044e328fa0888c1b70f660596fba58b4af5ced300f586a588dd6ccf57461cf130d77b4639c71b1e24e8bc39ac9eb7d286e43d565d3ace7c5b8e30706860cf86908caecc4058e5e64cddd322095ae990285ae5c04d93d04c79152d4637b65d4603650b5a1d86fcceba05df25c094bafb4e90d995d26995c739cacc9fbd2bc2aabe84b830ce6eab88978cbcaeb555c272e95cc8a679f66859b0bc50c4beb1083f65ddcf540584e304fdaf3e5b70ca84f446f6d036c7a0671661ddc09a8f697dca4e09df77a95b9d640c7d415f6953e92e40011c2b6f5046529e844d448cb9929b3bcce741ad22c754d5dc9a5bbfa8a3e7a4b654489de18578c6fa524471f3e8d705bbac788c715a427ed2c711600f922b2f8147c276cbf0cf8da06c199770bb61effd21eb0e61e444262212b52a39481a541758576e339bbaa89772723e22a6b78d50f45b24f0d7ff2f7d546c466b38ba08ec9c7d975bebe73afd015cf723a36245fc640c9620d7b9fdca138275c2cc03585b79007090f5ad6ba3a6d2c0157890af77e1c85da7b0aae30720c8b5757ccb4feae20da7dfe63103a17f1b99ac089cf2a39793a4d87c2990ee0b08100e5e2b4170c7e00414a38f57df676f6e7b07c51c78699a464a19e8c7eb90c20e99390f33360a6aafd03e5a394e81bad925c134f914492262932c975561ba3e940e51e63dd4bcee56a99150a54134688ada922b1167902f871f46816535b86d59b05553b0d304a40ad7dbed2a6308ff76f19945b0536ef385f5aa020f748ecd2db374ddec344d4ed9ee458d10deaedb528065f63fb786baf6a8c5e2fabcf8d37a49f4720ee54b7feec11dc10771f547b682a78596f31e37169028057c901f7aa87b4ef9a91614c84f9bdeeec858298ad6de251c95cab2af6372bf12ca1db515c7087e9f52fd3a7838d86a206428975bd17b95e738b8f9d9dae39204a86bd3761d92f54cc25ab5cd677a728da91ce95f745b569180504bd137876a252bdb73bca6f5541112ba0e59706296fb37ea2b91a77c151736d4eb9f723a2e73467691f360c25c96ce7959c34438de871a8a3df774426ec04e439352bea5da1b478432a7acc8a66fa4e2c159f6c708e620a00e866dc55b554a4cad8b2a0ef44303e3021300906052b0e03021a050004141b1b1149027371800965d8161af18c80000d03610414e18bcf67d6a00ff6ea276b8e7ceaddb2f3b7de9402030186a0	yyh6GzLwb+5nNnDoYMlnzEJBFK6vFmcKp1/42mVu7hmn58Dz4Ir1Ig==	f	2026-08-05	2025-08-05 18:16:50.138362	2025-10-13 22:41:03.318902	2
5	e25e9b22-27ce-46a7-837f-232923906ebd	QUINCHE MORAN LUIS ANDRES	\\x308223bb0201033082237406092a864886f70d010701a0822365048223613082235d3082059106092a864886f70d010701a08205820482057e3082057a30820576060b2a864886f70d010c0a0102a08204fb308204f73029060a2a864886f70d010c0103301b04147d9bf4fbdb33892c5399668d01933334869646f3020300c800048204c8cb13409551a03f2c0939b2bdff436861030f7de67709f8a49cfe1f8f1aab4bab02360cd3430dc891f9f6d3c804f703eea46693619e1c104e71bfd0276e2a3ccf513af0e69ea7617fb3ec2a6eebf70722cbbdc91daa68be49c0dc36636888481535622092815888b5e5b39dd2184480b7558b35faffda226953cc38195d9160d2a5a7e8ee154b6d0fc1dfbe0f4759202a73ea6118d847909bb0df9469f7b215890421b3bf673c4e3b68694abb841144cc316cda4b1ef14f4b87825df0ad3b7eb4749b6bb043cd77fcda4567f6f0c1bb03cc96ced5fbb96ae68696c81677d9787682f9ee02badfba615d9a733ef2178152c5f22ad68d3b7e6776bcca8e4e74d8c4f7d7e2f744a1e081c2611a0305f8a51251b652d3f4cb383174a22b7a40757a2036df3c52a68a4a4a2e05f64cb932ce12d0a8590a7a7b2ef445a44699c5eda645fffbd3e39b4a11f17aeeea54f78fcc982696424e77f3beb84fe07121445844a60dfd17982c53acd61a2259a8cf0c01218dc08c7fc1247e5d967f3932602bb83aa236410307710326c7f0a29d02cf0b6d841e1a08474273c2eb8e94399d80a6c9b4a94468697a93c165bb356ec6054fc985bf070e6502cfbbbdd255996cda4bf33f604d29f3bec148686fccb69f2f450b537634c2c26e412331b4cb00ddd1fa6b10125924efee206fdc849764e8e8631c35a9833b8179566e981fb7cef661ab643d5258fa41a3b0a3231da151f3a5da709f6bef0a44b1cee8c41ed0c30080bb69a8276341cc0c2c22d627cba1dc094b3e3ceaff0597c4f44435e42eb9c2675bc0bbd011eebbd84eca45d77546b8e1fb114fed4572c75bf23ee0c196024bd8a568d03a6468ec58e8ae915a5509fb58e9cc17663e2670f87ed03ef0643032ef90a06c2c41bc2e99a173f1f77fec023529995c47f03da2324e9ca832aecc2f86e33847a7057255a87a3f851bed79585c81371c5ec65f841de826698543c6048a066da2c78866f3472e70fd70107e70fd7fd9aa796a89741161cfe733c11d59aa7a1c8cbd0183640c7f10a5e194e1a8608dcdbba3c50b58657b0e9e807c0fc6f5d3d264f796384ee5c32b6a817faac1f4b6154fea30e84f3d1e6488ff4a39bbdad9bd7a47647385114424683c39a92d708ca215f7bf924b9702cb5619a65256701b0192f45e415f1e4d1c90dafab57fbd5758b32deadef3243c550c14d61041aa38d24b6622de48341d5a8f18f9897db1bf1c6edafb23192b8c4465f8416ba7637c2364d400d4a2cea09eee8c7c48f11bc6c0453c692967845999caad36c38664b7caa7d09c3c0062d3541160198b66c8ee28ae8abc6a68832a7814564221ab97209453d3cb0bf71e45b17e45847276b6cbe17bfc9a4d085f14085b8d61016671d2a6b87d27efffcdd10472d308ab2acebbe72025e61b4c39454bedbdb3a060df8f9fa4b781eb575a341f3070dc592967d4b6daa61e4fe4a09848564d10dc4adc0bc9a16c1c8c840b636bbdd7abf4264a859e4bbca59099d32eee607759492de565ff7ee1f94393535ba1516975f8a59e3f85e0678ee9d33069546f33163738bb2bbfe65291021d83abd624143c15a1d7753e03d428bbcc8fe6efc1ecd3e86b7006d8a62cf99576fe89957325a16aca8687d885f2fa3dfc563f7f7b2949bbb8fc894ad80a6a135687c71f1478fcd67a931d3811835776153e834ae2199ad482d8188264b6306bba3129613168304106092a864886f70d01091431341e32006c00750069007300200061006e00640072006500730020007100750069006e0063006800650020006d006f00720061006e302306092a864886f70d01091531160414c5cf923485f9bddecabe417d0c7787f49db6594430821dc406092a864886f70d010706a0821db530821db102010030821daa06092a864886f70d0107013029060a2a864886f70d010c0106301b0414f896e3f71f26ca73f80ca72d98dabf79b985f628020300c35080821d70839ba0a872e6e65c512ee8d73b765f69f2e5c3dba2132ef2dc75efde59e3ceb54f4e7caa1b946d15f090b1f03cbfa81713f065e3b03da8cbb708c57daf51bd7def5ec2755447042f34f37379b905c73e76f33cd0d2c08e7a4cb157438e7279daf192a1ffc70645abc37f53565b7f5af7dd7b57bd915ac7abc70d362e60139a95f75c3bb0308974c9d4f5cac45eb722f89ab9e2d15aa2fed1b662c3177d313595ff08616e3bdd5940e5011f5b9a3ff44010499287b6e3880da16da010979dd2cec2b5aa7ad3131c3a097115dc11139b6443020c16473b677e2168f5ec8e9969e469d1ceb994870c56b80570b678dc46aa6d9ad62c8734c64e3fce2c8198cdc2be5c2f16c1b8fc31e6e98d0e09f645679a040945536a9fca7b8d3cd7e00b2b3208cff8007a9046f619d3c51477c726a2213ed34caf1137249fc491a4e40349674c46e818f6bb87e2a053534b2cf66f46bc4b490439aed631d72723ad026ced8917fbda72b2f2cef4b4b10a5976834368c20ef148eae930083c00e1ac11791bc064664436070b797ff74c24c66cdbe740c0d1e24ee24592bb29e120fa3229e6f41f07d78e12a98c2f9adba4605fdcf42658a311d4ab0f9a07cc32e7c115788673a8037a50b7546d1e580be5120ef9e22361c86741071cd30e8e63d33d4928ed4efe9ba8353f4ce87cfd7bac326ecabe36b5360a6e998a7464c1416351ba4b86091ebb0e3d3406a9bf2111dfd7df4e3b6338e192767a1ae00e9404c0acfc132eda6cbc2a88facaf911d4c67e6a5e9630fd838e7ad7f59c3c1bea5150e5dfe589c848c9d86b5f4b881552148104d2ad512a92712cf23d8cff4e06ffdab32485eba4b5548896798670464aeb387299a07d1a470400536e338dc8e9adc545454151427825064452616b34e8e68afb99dbd6db4265aa1bfee01bca2a053ce2ea9ec6d9e0e93b23dccd09fba1493ccb94c0bf4edd7e82292568ab1679c5548c8e3811e15a6de32f41929c25d1b84925c46192c16a8f0960c138ce836ae93244f25d760530c5eced929f218a50d66050b17758d6f0f5f4bea2874d5195d7962f248d372dea826e2a5747c634556d977f000b02836da78b2cb296acbd446599641c4279dc665af51fa514f5afad04b2ebaa4029eb1ed52df1d99b846f3cdbc06708eb0f7362da6059a831167644c33c8e02500d87850e465cff6bad1bb593147b10ee291654518016a0abe9b77cb5ef1841d6b258e3f48d228aa444d261bff2d0a2a6761972fefe3245d40e2a8ddbca0ee2a70ad121368f3076e7ad30f0cddb1d8f0fd94493c32c0793d823bf00113fe9f475623821145b58bf9c269387af41519d00f81454e4bbf1cdc4ee983d5e00319b309975504404acf941d01525a4a5cbebb1214c8c2848f2c9454d891eab1e2ec4c116e40e88aff59352b2637699c109e4725cb4fe1825c61ff07f0428e3e4576e6fcc84d9cfce39a7ae787271a9488bce6571408af2edeb0214cf8fcadc6e3d901b23d8959fc4f0d3ceaf597b30ffc6b41032b09f3bbe0deac7c560f4313ddc385c6ac07b1c38726d10a6365dd444127e1ebfd6fe7bac65d70f680854633ff66d11a5c05800749a0d1672b1fa88722f31aeaa5b44c31d976fa9fe3d0fc6d668e17805762b6e16f281d95fbd6809d59f70eea03e1ee192e5910f22b12d4ac6655b98f7d54de7c995d618c499ffcfc2b8ecbc8429cea19621242d9024de02082238f72cc23fcf5f9ee2ac1c98b5bfe14d620efe7df0edfffa64fe9cffc2d9c4e63020c5bbcef9a3dda06598d6be5ce972de7229af8050661dbde321845078baedcd990113a1643c50ed27266445e4b3d9b0b78cc5be66baa47869de0f713b2c5c39e5e37d3c09fe002d6dd2d8895317c2ba793fef260514cf5563ac3289eb2cf9f72c700a09180f33528e7996346483f22533668e447ba56879826c9be67c571c84e8ceb238e0b85bb2284d720432fda5c677c79a29c6ea6a74a98e8df9c8728d7a7324e2f4438ac71d87c65b7450925b18ed3ab8471ea51723bd5deb86c5b575537721df01ecf308c47a0273a77475e1ce3d00a68624cd000a48ad3602cbb0155e0f8772976f0313271de2ce0c9a28cf78681712193aa42e21928e5e95f667aaa000b67514ab50ad0797309b42b2c09aec98d0edc1bffeea2d351a194880949fb4b0a0e29d223d3f666c64c3cfd7c91d64b62243b974a5471933633a96f9e50fb9783382d5ccc03771d2711d189270daabfa4adc5ce03f55bc4fcbc3ddc7a3408d657b36604bdcfc79e4a6996d5c1ff448e7b3594d1881ce3be4eb0c113f3482a7fab13ac8f6208529be55307bf0483709014f0c36739e63217f37027839f84ad7888d19da29d4bfb879ddcd7afe99efca2ef52900248caf9faf37b9b4ac420e5569024eed56a04efd6f612dae9550ad0734aa3ea5eba4d00e16ab595a57c1d02f19860550f198b719b2883e2b922dd388e70f99f0f6c338bd21e22934b1c3c8420608d677b00d07fe914f304ff0610b828af621e6cbce9ededeac29fb141914ba7d63cabfe1a867e63547ff923106585279a6674744608a864b6d219bf2f7104e47fac63d5f606fb8d2e063d154cb0dc8e5a2cf9727e4d64c9713782f7f8ba304f21f94d856a09548b45abceaf5be0b7bf330ffcc7bbd2cac08511afdf6548763831c5bfc5211a251d0f0fda6bbaf7cad81e51c0745dce52ebadc1db1f3a2c6f1d34313a2a222ccd817d8068ca188f20121032bd87fedf8148222f82071aa6dfe8b2a4ac05529cbdfc1d9bd48d3963fa1550b458fe850c5ec00b5da1ca4577a1a1986a56e05c14a9865b48d0570f21ae0a33cc27715eb31cded18bbbfd3a76bd190b020c06830a616967b02e19a6554b57167c2bfe2bc87939aece686966f403d3c21c1539de13edfe25d24b7283765af400f598f2bdf724c80791901af6960238382166807179694af43d1fb8247682cebd100416c8c44e84de63de82f87e39b2bb96942530292229708d31e1608cb9cf5339c6ec182b7e552337d59e51e3bd02be41211ed5a7387056bf42e55ad32f8d7b9bfbc53bdf78a2cf06ad9d59ecf3eedf3c745950b1527eefb51e6ed017ed5d5845d28ecd760df7dbefe0aa15e818479116dfb0956dba549fb3be06a8e4cd1818afb4484a0659f83c05ec03ce8514c67c6ce274a5680af978a3bde52bbb3428d256022a366e7377db24c44555cfc0ecdd0b75760874054935b7330bb561277d297c384cac4d42928ed42466e40cd1f5783cd734be3876b9d5de3f6af5943ddbd245be8d9a57925e37e8271a03b19f5f1509806a0620744f918d42fbdc795075f2ecd206af6012b039cd515b6f1daa0b3101c803170505f0b15228e05120041ecfacc78cb5fe385681139dc87858a544b2280aa3ede40f9a36374f9e4881eb671ec02224bb5add3c8a1b088f014a05af27ff4ccb10873dce993db7e9ab498a407566da04917dbc15104c65d7bd3dc10c9582579d72121bb765b2f4c7346a9d0505823a64c793ca40e3345b07d1cdd030dddebf0a2664e893c67215b53ee7737a8348603a65123508af6fce9cadaa3aacdf20434033cbcd74ad87230db149c3472548af12d23285180e06dbf4b45976715291d16879be93b229c0bd7e2dbbb6bd6f5d3246d57679f322a33584ba78a3c329a65d7f074a967dfc068d7e3a690a6314ce58dd28e82850e571826f516bad08a222ce86463a6f131d9dff8601a19b172c0a4a32f99bc6b09aff330661e3a38fb7dc09c0a54f16d1f45db4e88371fc8956e0720ad38595e21dc212322c97480f44c012fb9f55184aea798e0516ee874fd05b73b932d5a96d9f4dfc3c53d71ada261cde5480cd558a450758ae749a688353bb360d9a7cfce16ad464d36c199de36e9fea0509a6fd716747f1c3bbf0c955b4dade292826fd41b443100bed566686593d8b48b760a5889fb600c7d77579dce89d7e8c9596d588d1b4d7a5ada4296acf8d2bd9e68f84c46ade0459334b61d3826bef4f18bd359a37ab5fb3643022196d47b4d84287e58458ea8556d6a4b64e49264d3f365c497d60e38040d1f4ceff43b5e4cbe2c9d4a5780b6f09ecf88db8c2d94a3959ed5dfbd7bb10eb61d5df711cfa7425d043245b3f6aaf26a78128d4200d92575d08685db0ef6463f41c601340110978e74e2a2260a13281539909423becb5ccb2385a90755664293aee37f5e1a712f77d25faa69de994f7b3fca7c1c891e254653cee4d166f9ed9e5c2f9b5acae942b7bcae4b38487b1de6beccc68f496e885a206846954893f73417bf1f014e6bf806716934e94ceb4ac97f0fdf80c69e5ac0e80f157c1c54b64ae4380c047f5f27b694574d5c96eaca32699faea3d7073c87988468f2a1209b8a284c46cc05acd134fc757551ced854d81db8fd2c7cc41775a675058a89a54b6cf47a014032e64afa5c76f5f1dc8c76c3be6697a380b696e5a97a9c14de5d360816251412a3f86aaf80087071475a8f73f0b79f74dc4c4aaaae4dc8f200265f41fe184207cf63e115073e9e50028ec3c45d7e44eb1736f1c34ebd6d062480e2f3d0e8e21b9df05bd6e029e084d1dfb7e58084188366a81da18e65e8241ce4009aecf8ca8f78311e4322f6fd24b71dd15c407c8a6670af72e03069bbd79a0c3b07a4cd5578e0ce1979c86bb7f7ace1dd5ba305050acf282ba9083ace3c39dda7d5327ebf488c0bf56058e93deb7a5d5e4cf230fddc89c3f8ba5648ccffa8b4455a12413aea8f6dec8e06b44774a2430876b0059e63c7ac132155812827b34d5663046d6123e17f47e66af2b1fde1e1d2d58898b67c2d410755c732eaaab3d54d33f61ee4e5c41a5afcd8dc3cbf849473f12ba73b14dc58cc01dfafce79e387cfe0b13e12712cc62741fbbc61627f1b8f07a8e599e419751e413830f1892462258069d5da953e3ab0fea7f59aec30cf24148dce687c14bba09d573c7f70b13c091cbe548b6f22b459d732ecaabcf110af9e28d176673c528efea3145507a57560e70f697e0c297514c957f22543687b72026feda5a28f1a1c5ef274e20416c6df2971612f7053112f26b978f4097f8f4c921e84dcf8cae2557f0ddf022b52014787bfb09ca302cb8059af628b35acecd29c10b850d51a7158152d97619cf212057ae3990dfe7a7114258403b937b94104fbbeb5de4ee626701df07924da2ac08986d007b1227168a7663200f05610bbb7d18190bccd8868f41f5879521ea5887c7c4fe023f707d02c18877411a49d5cb7042dcd898a4e0893b058ceffea3612fbb29ff8e963f9d2e23ede62e67f0ba5d984d1c45f0b6a85c83f146c1747d08bbb3c1811fc2d26da7b91b954435dc0e3f8b9cca01aa1e49a2adb8c2a030266e5a9905970f39e63fa3103d0ee6b9a8b105dfc52785c53da821ee14825994ade6c2992000b648f1a12dd6cb614dc4173f5f0104bb8ca38dd07684fdef6c5395ef717b279daf38b2818b86da298eb5958ef6c25894ef992e23fe2429c66a53fa314216bd1096e3f59779fd19dc4e7ed0ad118dc1e517f4d26bd7ce1d684d6cb7a987f2dcc6a1a984faa9f70f55edf5888c0951525700ada004950f1c779aa30f924f2fbb78bcba9244daa2bdec4fd7a0f65e84c6d910f335c41c40f500bce22937bd9b00d27daf50594a6f454da3649d8fd80c4914935a603742d704dda522b2de3583708cb226d1936205509f13cf2c3b7caf34d042a94caabc0108d8bc701b22ca6ff315ec9366866cef208848e9690d9cc4fbc6fd0190a7f2718fd1c9ee7919d39b53d9b75b60bb0c3864b5086ec1da89ccfa09d5845716273c0721901554fa83277ff7d561d52b5c05f25c1afda8624c8ca2a708ccd8f2631567c1e406ecf282dcc6b2c1fc5c6d80af695bea3cc23d6ef6e80a2ba5bbc61a1f9c6f883600747d7edae386cc420a0aa7f6d81d8344ce30311c47f48bed7387e53e743060804e4631292f3e041f390c7b892b9c87b3f40fd6cb8f0126518ea7c806601717b01f8c8def5df774b04d0b0f4ce91ce0d0e151b37c05bd645fe69cb936e7e8147df21cc1cc90b9c7529e262c7619592db4864b9322f6b087b1d0859ef106f0bc71b2a25ead714b1425630e47a2b5e1d89d56594833f3d1f76dc6ec710bea5f612b9fcc08fb054e81131389817609f35e5c2d6ca3bd9b465e181287eadb3f58f85eec94cc419e8791740ecfe349299ef7af73a3a30fca034593ba6ab690a416f3d9822266eeb5bb9cf8419d395bdbc346e3439cb72bd3b9c9fbb12c9f64739ed5c325c371c065f4000c47f5365ffe48d565813482ffebc8731521c9be7f80a87db6e82855e5f8c44f7aa45067f2f6bc0c3890456fe99892a946d30d92b11338fd442f3402f1c38439f7036c2d4b22e92be059d6b677eaf97326b69c92b41939f34981c9b8b145dc764d9fbb32c83a375e2673532ca9a49b862525065e4a97b37e5f0971775d29b7be99a0a7d372a20daabe0314826a1317f5f370461eb5d2453a74b3a4622dda77aa883564d6a23a8d2b82c2f9b9a895e8fad7d94ee708c57fe79dbbf88868d94ee7dece5c9853987db184bb0e1caad331dc62dbd0485680e8c5a87ede279117323189d689debf9c95c149f727ab6efe0c253a15a9ecd5554804439ea91219ca506d508884d972cf4b89ce8c3e8e46d713d45228bc8972030dbab89366a348f8099987a42bc064c44980b6abf70bdc43a96732a574363108f2b61d94df3d4efef019697b482838759038bc32ce4bde7be9b00dc16edfc3c11ce6b89449a80f688975ae958e3ee3a4b78551601a77fcd0b0135f34d9fa0a4e906234491188757b3fec139b09b28d9c682d3ff19499864d9ec9c80cefd229edf51c15d42a154e040cced16a2b22b6d220761374c71b2c4a52fe71afcde6b53047c2f28179a0697967c21380b85482a5f0c7cbbb346783a97c1d9bc12f92ce48e06ae219706e295496fb9137a490f5bedd07003978b443ca8fa5f6c747176c6a79a0026ab43f155feaddcc180e80ab5452fed84ff3b40fad86bffb98d52e31768e4047ddd514dc3717c9f5c13f0c334a4e3a006201e2bd173bf0c6a0aa73d5317c35f84319a9da45478e50d5faac9c4dbea5814fe1d9b91c19364785a028eae095e93e413a0384116bf61bdb0bd1eacc3e611d6942553d2e79f2fc2dd4bc5c8b31b403da0bdfc822108bee2c4ce3f98e47faa6647781b75f69f170ec829bdc19e33f84ee4e472be36a6c55e0b2fddc3d3ab0d299cadea5574e2ba1745805f2d2b9cf7ab27168d169555b7d4e8e1d65424a391139f49d98c8879a516502b0187d0a3f81485da1997828dda8ae5b6af888f38e9a3dc2715dae767cd75690d745311a723ad343ab4d4c1b0f8030dca64bf96ce80a74297efc44bab8b140a2d2a47edec52f465986424a176de43b89e38eed833e7d44a458f6cb86147c0f5a6b30ab45d856952746a066d25fbc9224d57173f83ab5905f976b0047a54d82f53aa50e38bf218f146391b3d3350b337f98db2e87a7e6b8f302ecf2de2021a23392b2122b9dccc21c0ffdcaa9e9478682c94b86db13fb28c745dec0825d3afff2e8c98cee4136369d350d7f3c77cce7479398024b55fb1fc9467dd13390071325da113fff3a6e670aeb14eb73c8c04883bc755b4f1f108d76394d14b4eb79ed285f2e34060c57c68668e94bf48192aba8bc0d7df5d50c34c8a497a4c105f61322fa73d5b7523a31dd51a11edbb9499dcee5e05edb6441bc3bf89a1697baa010dc26a7300b793b7b128a0580a4d05cd32500ea23e586cddff234c5a9b2330901692dd1ac8871f237d0930e7ddb71d356b85e55695d4fb3c5a505c5ce70dd31e872b4b3edf17e29363bfa383e20f37f0a35033a93243f650b4b1639a12fc88532eda93204bff02625aa8297e5bd5dd0f3dd35b03f6b0336c4b65afaff8cde6b5fa13ac26575a4c9ded93fa507c09c66793bc22ba70f4cfbb0c461388d587263dbdcf5206e4dd9809f5e6c486bb03944cc62b428519426539393c3b2c299c6f95151d63512d9adb994d86e421539c8ca4fdf6f8f0c5281707b5cf12af518b225f8cf7fdf87ef7c368bd8693a59ae31848c3fa909a628447d592eee7a44d7d7f562c1342b34fafc1ba72ec85e444881a84b59987a039d6b5e2b4ca24d6687c9a786d639f9c30294b48edaea88fb510c1587927a2b08c45f2c9aabae2b8185483d8a54f009007e8401c7e62f099ae073ce84ec538033f571b7b6889b436186d176b92aeb832816293c42b3352ab01049a031a90f8ecbc4927bb0341ffb9694c8c475865ff65668d1b1af494bca46d3bfc0619a6b020c869b3cbdcc5c949e175088b68bc5bf1961c07703c84ec2c5e3762c600a1e5a617ed8e4cc5d2e7578f363d589c9a4966bb59bdb527b2bf24ad7c9ef774e3cb5dfcd250b7e4932471f8d606b1ba54c07842e30c2c19068d1af43e2f8d0cf86af0ce86e5bef16c597b3ea65abc6047c99fbe11b30fe2fbb2e56fe67c17a53f1541ab713a948fbbaa438f5bd6942a1bb7e1e7d1f36310d137010dac8702f8727d8f81c5ef65a6156f6fce6d6e6d96d7f225a3887efdb89de7f280aed11dee6af3a8536fa0121a7863ea6dba42c04bd1c23e62362213c3c335f075a586812d4579478ae4a95c176aa338df612aa977f52bad6c4a06ab1f1ae2622c3a2777a05dd34bdde227350023cfb05e4536f28209c99bd1d232881b9e789fd7c9fda36e6c22299a3f3ca1b6366fbd3ecdf41aa12f3ac6e32ba9314aeb051661dae798b0f2de409f952bc792894f17b3bd8837ad8d64b8e9fcc5216aee52512c858b5b739a1f25e5878b6011ae4dc816d850643b2b5ee04048c68a40f1598de0b9b43d9b5cab047ae3ee147602e65f783bfc108c35e93ab0c9ddd43c8338984c6a00a3d816002ffd1c9bfd6f92a569eaa1c667ed64006c4faaaa7b9fc15845ac0275ab3e014eb4e1c2987685a1f23d993dda772c57b40d2d9970ead8688843214f1dd01c2eb3609accd8e5885681236f059c59123431be456ca07fe0ea843f5c4eab2b200126b4d462883e3a1809f588fcabad16d1d92208824c0d2e1ee0a3477664d97746f0cf88c6ca37d5b6b6b0dc2dd0332bf149c423585ee178e6919621876722de840e60afa43d30c7e7c54b58b251658b1b620795172bde19379e573066041b6d8ac6c822194aa5b59b95503f98ef81d140a3c664a20d500ddbf6ac753269ad4900f50dd7ea1146b5c694314f685dace621660b70d5d00fc3cae913cdc5c227fa776a3db55c791e1ca7084772cba0b41c9f23852914c46656afd98b8056195bf8866a69973908d57b4a3f11f814e6e457b137142e50d96cb3c2ffdb6c9d9d1f0e572a9c7c58a866b7e432649a709e05c2824b778f88d96dbcfc5efc9381f79a3d1b6a52f5bcd5c5143660573cc781a4f5161bcb3520b83351f5157905ca18f6364373040269189ecb343386e72adbd4e6107b31605df74758b08b044e328fa0888c1b70f660596fba58b4af5ced300f586a588dd6ccf57461cf130d77b4639c71b1e24e8bc39ac9eb7d286e43d565d3ace7c5b8e30706860cf86908caecc4058e5e64cddd322095ae990285ae5c04d93d04c79152d4637b65d4603650b5a1d86fcceba05df25c094bafb4e90d995d26995c739cacc9fbd2bc2aabe84b830ce6eab88978cbcaeb555c272e95cc8a679f66859b0bc50c4beb1083f65ddcf540584e304fdaf3e5b70ca84f446f6d036c7a0671661ddc09a8f697dca4e09df77a95b9d640c7d415f6953e92e40011c2b6f5046529e844d448cb9929b3bcce741ad22c754d5dc9a5bbfa8a3e7a4b654489de18578c6fa524471f3e8d705bbac788c715a427ed2c711600f922b2f8147c276cbf0cf8da06c199770bb61effd21eb0e61e444262212b52a39481a541758576e339bbaa89772723e22a6b78d50f45b24f0d7ff2f7d546c466b38ba08ec9c7d975bebe73afd015cf723a36245fc640c9620d7b9fdca138275c2cc03585b79007090f5ad6ba3a6d2c0157890af77e1c85da7b0aae30720c8b5757ccb4feae20da7dfe63103a17f1b99ac089cf2a39793a4d87c2990ee0b08100e5e2b4170c7e00414a38f57df676f6e7b07c51c78699a464a19e8c7eb90c20e99390f33360a6aafd03e5a394e81bad925c134f914492262932c975561ba3e940e51e63dd4bcee56a99150a54134688ada922b1167902f871f46816535b86d59b05553b0d304a40ad7dbed2a6308ff76f19945b0536ef385f5aa020f748ecd2db374ddec344d4ed9ee458d10deaedb528065f63fb786baf6a8c5e2fabcf8d37a49f4720ee54b7feec11dc10771f547b682a78596f31e37169028057c901f7aa87b4ef9a91614c84f9bdeeec858298ad6de251c95cab2af6372bf12ca1db515c7087e9f52fd3a7838d86a206428975bd17b95e738b8f9d9dae39204a86bd3761d92f54cc25ab5cd677a728da91ce95f745b569180504bd137876a252bdb73bca6f5541112ba0e59706296fb37ea2b91a77c151736d4eb9f723a2e73467691f360c25c96ce7959c34438de871a8a3df774426ec04e439352bea5da1b478432a7acc8a66fa4e2c159f6c708e620a00e866dc55b554a4cad8b2a0ef44303e3021300906052b0e03021a050004141b1b1149027371800965d8161af18c80000d03610414e18bcf67d6a00ff6ea276b8e7ceaddb2f3b7de9402030186a0	XhfFKV390EX/JrJRjPd47nGkqFZJ77jPqeG6wHe2mw7vyLRMwk50hg==	t	2026-08-05	2025-10-13 22:42:44.292425	2025-10-13 22:42:44.292479	2
6	e1e4ae9d-a510-40e5-a925-d3e60f1116f2	GISSEL VANESSA CABASCANGO ANRANGO	\\x3082228b0201033082224406092a864886f70d010701a0822235048222313082222d308205a106092a864886f70d010701a08205920482058e3082058a30820586060b2a864886f70d010c0a0102a08204fb308204f73029060a2a864886f70d010c0103301b041492a1fc51530128578299059fe17a5851f01fea78020300c800048204c8d8eee572c15bdd380d3c71253accd8f5655d439f62b77b13182ab6031165a7666fa52d699b06bcaf1182af7b71819387aa495cd859212b1131209946e6f5f078d0cc9f35ac07e6d5a5c3c0c8a836ac6782cc41576b4caf579517057a5416e7a62d975d3196cd0b110ff25ada84cc35b8391bd48f443f344a0d03afd31e3adda706c64a92cbec40295a8d8cefd6d6666f0402a7bf8084608e37d8ffaa4a72a43c888b74a6aaf6971893798327870ea65c7dde31c3d635948b103c7febed267572c12ff2e1ff35c39ffde1bfa2e5c14818969bebfcd6e9174ae5d161b88ad2c84d6365118e57f2108ef2d2fe49290fceb145071fc7b474399fc237565983e04a0b4fc375215bf5d450cea38a3f3d763c6ef263b2be4839b6af386d0e72a1f693a7a0b67ced4355cddc51746a7e5b3d71cfcbd1c36c25e205cb5e1083edeee8d8031c9c574c936e5aa2bd0da68ede696df91d8e9fd89fe1ff2256a777481e9db159ffe477d0fc62c041c46096a637112602d331512be76d728a3c4a081cb6af081af386991c11279279a256a1d853cbbfe2df1262f2417ee8f1f0d2e19a8c12c1f7cce61eacef96de240822a2e6c4fe005e8deba123b5f959fabad0cf93806bc4b8a480237840f8307f6140a349c26f139cdfce746801453a2f6a6e0e1001bd26c60e6c2df3cc9d907df1b94b05e066211769b5bbb1738bc268078df17226bef1375509d3fbd7c34c9559d56d7a7ff8dd099c1a08bce2681c674cd3ae90afa5d9c8fe4e5ad7045d09c38bb467d2c3b86faa43c1b9244223ba3910af2a3e61038381f3bfa6853ff872abfd4c8e208754c3288c6b7c4de55f9af9b3d3d858a02ab521b1c17fb63aa8693a1044c989c9093f2dac4c86a5544d5568b1f4f05de99bbf760847fd42eb6a051e26fff36c890d563045e0e3a9f6b6c402ab46902321cb57e90cc768f59d447c55ac3113ca4e78e326bae068b7bf08017ad05374417f47c0a0a1b24ec291ed309d0bac03f823bd62160986e93355949401d033d6f8cd315ea8a08f52ce06ed857fbaae4ebd0b331542c80fe0266c319e00f34586c46ffff31a4cb845a2e64f336f6230637ff9576f29dee37c218ab00f6d8723a0a86e7b889205e0b3ad56dd015a00db98da7dc7a68f1605c75121a8831aa64cb73b3e4deb28d050d1a40975dcf47803d36bbf6e45c3c4ebe4352d400fbb21a8f8cea5ddd4fef226fa03f8008ce8df3d77c277a5d91e1003823f5412728e74500624bc65c71d3ead0ea7538874a497a19168bb8f693609b585b5ab5c7765df3cb0b35e5de57c1c944a18d50f07f4592b3926c257c54ebe876406f8a619ab798d1460f1c6fa0aa0a37aa36481d9451e7df55921788f1089c401c3cbfd5c87321efea7c68e7800a28ca1cb474e1a68cf76fd4e232d71eb900e439c194d92009134fbd82b373c76fc83d65d888cef6bf6462759ef355a7c44a87d1b8700f455ece3e13cda11c7581d3f9468dd16c70fc3dbbb18d566a539835fd4fc900a89342d178aaff0e823c1407e72eaf7724468a680a9eede1a12433b0e1f1fbd536c4f942399b3dd58b70c6f0cdb8c85cbf25587a0606f2f59d561389878e8fca349a82401759384775ed1287df25767ced7bca4358153c8f26efbd40e484c925c5c3dfef86d466f138f1f8269d8394de7b30daba021cfb65fbef6c70d903b50908904c58e4ae11f965462b84e7f2fe9df245b3178305106092a864886f70d01091431441e4200670069007300730065006c002000760061006e006500730073006100200063006100620061007300630061006e0067006f00200061006e00720061006e0067006f302306092a864886f70d01091531160414589db6f32a0a6a2efddc56cbdb69f5b233f5745f30821c8406092a864886f70d010706a0821c7530821c7102010030821c6a06092a864886f70d0107013029060a2a864886f70d010c0106301b0414ffa24380cb1da3b81704649e35a1e0512f2f9fd0020300c80080821c30ad13de428c527fa9f1a28f75839c7640fe9dd336b880be70da17b3f2caed874a3c2d7bb18a32589d79cb647938e177cf240ed4389011355db8c9d16b62a829892c9e454f4ddf60ee4b90799b0f0ad1cbb34572f95126a5a6b09fa537b01af3dda1bfd696b1adcf2dda858c26de4746360496cfefb168586552dc8d3ed0923d2de44f43c264ddd37c1282eab6995a10a5d92e980de1835e48e6d0ca2bca4f1a1750ab7477c10ac56989c9e425ed734b471d4a11d0684ddbb359b9a51ab4149e2c4ae1f5fbd7176c67f6cb829a643eb103919fa54c491201645c655e1c2ec80a643be7c813f222b0d3f93fbf405ed3cbf6c86e672443d4052d25e722efb38ef7a15c77a5b402698bb5a430a806bcdcd64292bc7d3ee3e6e429ae8d1ca6403bebf97f4551327b6ada4c57a03307f7881df27ff801dbeee5632a534a115287e1aab11a5523b0bdb9b8852c600c1da7219a5b03239bf690525f7ea6f45381c17fd579caa431706482f3cdba6b307e9fc567ce8da089b5968481d617f4c58c1bf97c88dcd34f8359d0d06a224e28ba075592abb790382e29c522c35c8efbae2fbd86372d226f82c58abf6ef2184458b623faaaf9f033bfd28348d22412a26b50e856bb86c8f8e33a3662064ae11bfe55110eb25f82fe32bc7e10b6a1e92cd2a01736391f75c7917391494865dc217dddc95d38f128d3fbf6b9956a6834a4d7f74e883324e0c40fbc11e4e88ebeb3ab6d2a36320b667c5e6d81ae7d1df9a35bc911cc8fdf347c1e49110a022022bd1eeb18317a282f7c78efa3609250f77252ceb46827c03616192f33602d0b446cd4c09ce4218e4ac0a4cbbc6c417e3ef8301a34ef4be83008070c93cd4266fdf0390f1b0fbddea1fcf6d4cf2c5f753773096a45af91f8cf34ade92ed0080651bb232e6994cc06f1422e52bc4f9b469f3b67c08c050418540316a78b5a7eca9a71aa918bbd0b8902dd7064f508728ba1759f29ee2cc2a7fb886a949294e70a12c5792deadbc4fead86031ebdb1dd12179979344d704400358b6554406c80a19814b5d2c992a151d1d504cbfbdc11ee41df46c26d5b1fc40b94a22f77b6a2b56c31385695e6b6bf23f31b5abdd304827fe75dafbb6c318042eb575b8f26dff8ef563259364f0d1971c8b9536827a645b234e99e54565192cf74a544153ad88da1a15fc07b82fcb195e345907a4594bf0d49e0204a465d40502107ecc4214c385bc00c0ffd2d4da9a3c0759e7e7d4d1b30e98dc2a7f6604bb6d547961a8e85e5f4d1a57f507888e20f7bbd97b146ba0c4965812483ccea4d403e21010c2dc99d4f780f01035d1a6fad5c27b637bf34110af2e4dd58533e33e96a9ae3f83ce3a7b534b66bd1987a4fc9ff589c96843feb3d4ac64e32db1670305b9482dda349518a51b5ea5f48211a0d5004af1e62246d1422fe90f7e4a6f63b19f81188e073ddbbc6b520bbb0f6474a83b00f2fca876e2c4a0cb7edce34f9b3edb6aab7a58143236cab09adce1a2444c71a89598568ea2ab5357d85df099e3a3442fab77f7161b444352ee19c0f8dc7f8032212a91b707be8c96f2d34c29202a637e719da0ce101a2c797a30060ac57ea2bbb25bd657375b38ec256d183d7de9293d7bc8a031d6b6c22214702321a11306ae0e0c9db864aa3e6e2db830202c57cb37b2d17e44fe50d2ece31055386e28d2677a7a36257d7cb1e629ae7fc64afdbe04f9973f6e384f3e804a3ba4cda4a07b0a7c8c6a46cd9e3c4ead587a6e204cfd8aec28da78fbb9735fdb9f534bf4bb7e63fdb5bcc92be8a4232d972613c226a3f137482645ad74efcb769d5b2251bfda473e325a7aed76fbe0f2b5ca4b1754406acfd298d3aafa50299b204389c69586f4daa70ca39d6a6a524474017c7218aecd06b2f05d9bc3bfcb51e3a0e9408d5a7b00e655f10df32bb6038ff78cb5d5d043c415c5cb59a84b9d2ea91d9b038212a11b7ee4365e2a8697514097b827be5cfb781d345441d9fae1c22e1db9ab1ccbca01e957e1eb2d7d325f2256317087212f74c907e9577ce2ec87d1e2f785c9f2df475babc46230228b01f13169672fe30671527eeb461fc3bc4b1e93185ac39a2b2920f48a9a7a669263cbd10b6a1b6d5ff04b963d8ba58d52bc520c43a50ffa0a271da37fcc8a1275cf0f7ec0098f58a5c392f4071676c7e2f92973310e22f4e56f518415a42287466104814a04049795ace72a2d3b111341baa63bd1e74bb6920128a3d3ce8785f66d78a5ef985d2785dcc0ca1d7bf3fb9861a02b799909f0816ca334bbb6cf566417b4faa6506d5078076c5456619c8ccb2ac7a0d672b497d71e93c2c370a14eabacde06310e1eab03570af014be7784823a220d82fbd3bb72bccf47b09b246a76d44608a0d1459941f2804da7de10825fd6d637f09c3a4aa529c0922d1acb871fa045b063cfe6f864aa7cc65869e2521457179cdc6b45fa23c95e184de2c38f780757addbec6a118c095bd30e454d1e44fcf81a00d47541f6f3434429c5858d989cecc33a9c00d4d1d46afa7d82017bd8b6d4305abc301b42e39eaa3a18970fff7814790bae3454a05734246dc880d8df1fc3eb5916c7a2b0b96bf8b76ff9e04ca14acb79c16475cd1e91a45946f48ccfbf7fbcfbfa855e099242023198b48fbccf75ac3057f21aadc71769fdac1c8eb503212fb7684e5d75e90585c565b90c64f527b75d5e08733bbcfedc39ae43e48092c71e18e823a728eb500baabfafe71fb44ad343ea95c59579046343e7a49773fc32bbeb46e39fdd612cb14b83c1e09c35829dd0b88348631fc8e3eb595b229aade0bbd8813c9c91ca1c0ee6b70a90d9680b8cc5c4abd799d5625bbb5baa56a58ba053a850ac79e245847663cc0a7e869b3f20127407e2e40a6c827b80de16b62727ed7241ac701ab01bdae388fcfbcdb0c5e9ac7bff30dbae83cb18efe07623690d699fc76d2636a9e8d8337ddcf0b5252f9393fcc8c71fcd94c4e888e6e2c4266c57f2e5ca1f554cc3e09c44c124d9d21c415456614e542ea92c32590bf4ebb136d8c051aed7b0e13c4d9a1922bf7722c7cb7853254e3ba13e9caaab91f212ca6241bf1042b982dde0276619a291343fe696f62b7ee64f894124cfc657270d0c469c1659b6ddaa84878bf9440de520ca89b9ea5060cbb92961d135e48686ba4284a36f2675f99e63ce8e09248b49ae7fb0c183cb9b2aa02523db2b54235ec12b76c7ef214f6fae8ee19e15810b3bf0a9d2f4eda60f978131862bd2e0344b196f553b76f03d9dcb052481dfd0eafd24749a0129be65843268636782c902280ab1f9a6abeaf1a9361c640a20a21c9280c6d1f33c1be85f11055e511f6e3f01b9584a555d6782f4e42b07414a481147d6369a01730321cacac7cc8707db10c3f28c6943258f677a9b71e31635aafadec4d8829029876a1489b77e3b93a9ea2ee75852d95fafe9d644f9953c3a034b5c73ff8c4b19bf27e0436ddccdd3f950e27d5fe3ea5116cd241ef73c5cd2fa779975e38af000a43e1e48ac7257419d5f12a8e87217d1dcea8e31839955b0588a87eef5fcec25f8bac271a3ccc00374dc42f8e3ef5ef5a88597b6bcea38a9007f8579c8b59ddf9e8c77c5866b3abb15288764bfe39b71e9929e86220463f89a2d775cc05112de5c7454f5ce23d50f9f1e40b4a45ee595f20777a54760c5dcab2ba488b2cc557bbbc5e1e362f75707e2205376488eec7718329e1389b49df3a08fcd06a442832e2da1ee898240f237b42f6c57c106b832c7ba871cbab1eac5e2d062fec8d4f417b0dae2726cfb08e127da6c898405c7097386bcc4ed9f68162ed6253e1e8c0c085f21bde5c14694d1eff3a9be679bd754f53f4b606069e7a3f882dd13b78194c9cf21d1c9ed8521e39b92317aaf28f1fa78f7b623dc299b8bd9925e7f0fd52d658740fc57fe866df15c2da5a5b5479ebd722cbf4292dedaec7412b8afc2316584443afceb969676eee0242fa83fb17bb5fabe60ee39bdb005f88701c8f6dbdab346685b362930056fa4a287a29dfe0e44cd7bd26a5cb3f4913e29606271e755cb8eadb50365ba674ca970910a68892b8ecd2c997acf61809bbc6e3045ee37981a2011b45f5bc6ea6f35ce7ce619c7bdeef010ef470f3fbbed2d0c3f175bfcf26d4b8f6727d1c133f9c5132aea98e70180aa21aef5c64124f775c62197702f4951ca1d873a579385abbcb33b845aa0a67dea848fe62da97336e3ab96128bc74ab77ed26a0448835cc135faaf015123c15e7668fea79ad8d137acf77e7eb7924c65cd744fb4c29986056377b3e951b64b340dc38c0eaeceee75be91f82943dfdd2aa025b9005b35ecc25b9eac9c40c2fabd2f62711dca904f5a69442ea8864fd3e17d9c14037ed4637d036129cacce72d15c92e156fd928afc98db02af80e0ff3b884f13fb47393bcbbd9c350d4d41a3593498ed0685ee7d4b1ddd4f6a81f4f6cdd4f659edf2e5b6aada8db92a55ac67e6e882abde0eaceec828a5b8197dfe3552d0bc8226a95ff6288ccfc76f9b452c120f3138f732d7d9138709dcce4e0c5b9e07912032ca03e2c7a4eced890bd197011beb38680e74b35c08b6ca819747a7a861e86984b7b552b4c62898c28db4fe79bc8e434e5f025e68a6ca353ce7bc13a2ba45a9d679999ff80e66e110ba42faa21e05a80c43b05ef96bbf022fcd713072382d271ef0e28115af2f8b143cee4adcb47116cbb0376a859168b6ce7d89500c58c14e467ed0502e0e4570508e83a9507de57c171add8726108f76c0404ed1de841cd1fe8e7fffeb45b57df699f858db6dd0bc34f9c27bc013292b2db969cf032b762c8e46e4a1f454725efa06afe88fba2265ba49f14ccdb2e804829757740c6d4ba4de42fc2582f65144695372e4c5963d93bf0de4c81c39cc8526d3fbbea0e1451577326999bf6ebb431fe0821bc9dfc866fbc53d0538ceba64670fc1228c589da822431eda3bdcf6dd5b2a874b2f6f7d0e8503e370cfdbad44428f892d34ea3a15680c82b8113dcd260fa2a57b6a01428dbe5164135ee75bb1e7cb79dc2a33d2bd3d374c5aa54eaeef73066f96cfa91bb21c0f1fe1dc418db3c8e7d2b18b25417990763366010221147a79897065f69934cefd2e0ac6777f059edc1363fdc3172ca754ff39c79cb3af4269db4f6f8e3bc68b2a2f03f34c5527125cb360e9c941c4fb203547b03501b814a288898a7b1ea78edae356d6f18e475810dc0d0b886aaca04755644e8205f786928a4a37f890bae1fb15ea46393e91c614ca5bd2840db04f84685544226055d423c2de2870103315e660eb5d247cc44ec73374d7c85c54d1a2e6dd63ac2fe46339e3fd70fa7fb8d617674c92867455f6157dc3ef6457aa8de3858fbf960c18977bb1aad62fbb28350a41ea02c66535392ae55286449dd3fbf080807e1915271d7278d26e6d356479b7d47160ffe91e06e528ed430125e645fff9f9d2b408346e9b3f09b8729ce9568d6e6e7b359b528ce07ee91ce4b09f2323ae3c11fad623e10cd7f89776bc2512347998c8b84f8e4d807218e1b5ff5107d3558769b63ae2fbd68d254fe7bbf4dc6dab85f8e9812d8e72fdab2905853a1377588cbc1781d0337893593995e5b2ff59e60aaedccb1d44fa76b572e962a9764d3c7a0bc1489f65c1feeebcedb5468bb1a19bf517009787626f150c9f8cd670b16c1a714a3b483749864c395c17df1dded1f738e8919e888c052762e43d04da1b126c4113bda2689523a40bd14bcbc115e21e3fcdddcd2026735978ad54719030ff1b11a0897231c95a464aa77e9d372e8395fdb64f149452caa608d9ed11aae966d6585f6ab269ad5764931475e9018edd341cc13850020b5a6bba55843e43bdbd7eb4f2da5fd9ee3d6c02cb48f47f73143319724ed733707331ca31f0b58d6b2d1aa895650a552ea7291526fd068b7b9a362a3ed4bad1e76cb81a851e75ac4b67c8c1af3d30c510e1820c4dafe1271a1a6865b01a7e5565962a995c495896f2070063f166733e9307d2a24502c9ec4526d2c4851c7ccf33dd3408c7f30049481dadc93a06c8132a97a063c13750e5ffb4ab894a7fadb08b71e7c5810b13ec0c883ced9a1ad18d4b8260322af9314d078fd0db7a924fb60cf51698e6bd97cfdaa0d2d0369179a59349f37033c7373abe4fadb02d0ffdf6afa6995843317a8b198787a42402b344a0fed6b06a9cdb91654489dbd6e582750215fb819f46b2d1ab06bea432d9f6380d0d6d2e946f7991b7012111c019e6de79a1c27f0083f93b8fe7c679b493229917d05478d89b21a8b0e3b4a85b6c3c0aa8460163c776c9934f1e68c776d1404cea4ea5ea9ce8abaeeddacf8123d40e1a52c37d247cdcdf51447383a188164e3e54dc2e7ce6ac53eb0621762d10a75f691ee45d6e95469ddfb3d9951967d414fb2db3aeb4bad2a30f596234351a30a7ddae07badd6c252ed6155aab9586da54eec90f931c1690c138345b40934ac6732655070ceb4fa35367ef0c03622d9cc22ffccc7bd9a76ef7b51279dab02ea95960ec630a2678dc96fa71d852f1c54f2259ef058695c35881cf45595fe7fdae91bebfc5eff34f109fe29eedf8923bed72907ec2e89a69cacfaf8070b924b671c07162c0a2c0c2f0506c27a2a95ca434b1272a6381fabb43132b4046937972d57269cd00c7d0b93c20ac54329f7e743fa6e49b8aa142b1fc0f1b18230a0e7ad1a14786748c2508f2921f5a96c269f124c62387a58b0940ea1288f1ead14d1aebb81da6c6abe9d99eeb0898d4cce8601495a2aa5d2cb1d3c322e793e26545e50f4ca42dd4f736e43adc0bb0e4b81a647d0898e026605c0986b7cd730fd11cfce3f5afe5564b948215f40f6c39deb19cf804383b9809ae07a61871cec20d4675fc81885dac99f52a1e0bb92cfee02937d4a83c8e986c1481384aeb54db56f5edcb9457ca08d36109dc5f2a9b539aac303825746653d1aca13c5ba391ae6c7b70b20e1b033b080522015ec835c872e7bc4d1de049a9592543e958a707c277f65abad7ef9c9a1afcfb6a1fcb1101efbe52edcaea7ed81cf80249c9d9d0da20ddac63bc8ac50ad0bef0d29db392e5741e0849e30feb051b2ac499361bf43cab9a26a178776da80c99907e9402e5bba19f5f5e5cab7d1a994ca56aed082702d79406be0fa8d34e39c59ad47e2628fe1d3c84ee97cc7d7f101267816ab8e170b6493b047909e44a78ecc0ac2579e77a4a8fbcd0979fac9ac865cf1c39477101166deef146a44e3df4a56e04d26ad974aa196d63f5529d7aa1b53c167c19c8de15f3a256a7fbfaaa671a07e3a48610c94c3ee22dea1e0f7d0f3885a115ffba2eb7b6b4647db39731514b2cd068b62272ec6ac4a7b7759dc3dd80c7110e7fd5d82a4ce71c413cf002b34376078a3a9613d58846b74f38028d2d2ed29d5a2e19fb75f8edbba5c7db4b64243117d890ca6bc6ac9b0cf9906e89446a7ad653c7ecf1f9dcddefb453ed7ff4de9c4516f352ed401bad2e185f20a0e561254fd2d5cfa1ef899c50c1155366eedd3837042ae827f53ba06f5d3fea0b0c88abea7ab47a80287ee66d70cc2044f4ab71fc63b2d04ec175d8292d6de3fa2c42107c8a1badf5dd55d689ec462373ede2e650fdb5a317f9b982d08b5e8f4f437d6deb27bfb1542ae47946a4a1fce93301179b04deeaf6819e50528ec03a28c06bc8347f1d4d8067a5820ef489b4638b7f31be7d8775744f05fe4e22869cf99993a9367725fba2fad186476660a670d12566700f27251215181011da4ba2703aed852b86ca0a7be63089401e6cf3381aed94c30d76ae07558a7a428ec36a0a0b01b313eb6eb85cce1456302ea94915ee106a114eb963c5e18ce7c8d33cd9f82b41b6ca24ce28a245595761c9dcb41cbcfd6a4cab213187f8d2e04e9a2f576bec1c3d3b9d4af363017e536b5ee56646fafcc2950990987a2febff13652044aa52a27416bd4250a4c31c803ed8bbb1bfe35f0c0ee450dcb4997a0c598d66f4f619d1185c1ea8bb949791fd0ee19970b705dbb798ba3eeb358139f8de4b2720bee95f48b3163d23c9584afc2b2ad7784b8a52321694a000443b4f29d6a6b56e21789fece9cd1ef598f142d14fce9879dd62de3e4cce1912740dfa69f377193df2cfcee360aac499557976fc6185d0467d638722f8c13a4d7993a9c75326cfc7775ba675f60abc0dab4a829dcf4372222aa6e4c914c084ef2d762abebe9f4c38014b64104d2de8cbe35feaed1151f7c6de58bbe16dbd1189db6265be95ca2a1e60153198925b95caf08d8e529b89bbc19ff9e527d8eb3b5b6eff45ca20c8c8e96521d6cbb92f0f41c72127480ac222041fc719878810a6218b9b4ce78001a6b3d8c14a1ffe26f83fd3febddfa08704670a95440df027d3a207df2b97060fd5c32e0a1155f2805a0a3b85d84d6b1f03cd018b0e58e9e38440559998e3a6b7db34c8cd91cf01b208dcdf8053d01e9130070430233f22e2730437e0af3599e41692132d3fe82d96ecc4d85962ee49e2df628ea020a16c361c831ab03a7e494e508840d438cdd2ac8b4e40d77b4ec4af62388de4a026e1ea80a8a7c6d9783454081cc90d0f64ace4527610a685970f64874c2aaf3d62836667d3e80c5a889a9ae3e19a286bf0cccb17c07cc80d6137497d0dad3ecdac6500f0c4d90fa2151d69891095e12840bacfb2eaf93b7209120e664852cdf7f8a35b30079d0521bfd46ba9926bc81bf990203b3792a23ae7e787fcc3a841e688b1e4dc5bc2aabc34e04de788552cd417ec68bc40b73ed13c4430a12be9a0058ae40f6cc7784827355ee0ed84e0c78daafa309d6110da7e964345b2cc74f5075655e9a9cc447bf17bfa82020045e18c9e3534729d95327a3268746cb31e01fde54ace464d05f8f098dca30b79c6e435b86fb0de771392cd23a965e126570323258dcc265f46344c0645d5f692e34fa1cfc70a352b7e9597af561b05061a006f9878126e18a2f2f19a828c5a4ddc6ea5a88ad938f7655d8dae73f398b721a146c510a3fbb90990f49d4cea1b17639b0f7261ae12d41316a4398d21f7cc5c442083f9021df747616f3eb8a54859c6efd903ef53b684df481ccd3d93e73ee7e90692c0e89b4e8f1c66b23bcf5729140a35e4f2570a543d7b72282b8c2474aba0cd4e264a900bb52c2f844838f17d5c1b2403ded0607af428e554dfcdbcf7efc00514a0ddcc7d5887aaf6af9f6efde8e7d5f63be7f5bcd4a6f2e7d5972c1fa94ec363603632e0037a298085e83bcaba0e4344d6a3e3b827a6cb5bef831f54423475056bad1adee9ae7ef05534c7e9198c3e461e75be1e711fe7e49d509b9a84543a9a5a9aa332e53311ae4e7c5bb422f5c130a772f7a59f72ee24dea91b23b62eae2eeccf27ebddb93f723bace26b8f8238859f9e61e30e51b92372d30b89ffc9f05fe6c058fb408a5ed5b11467be69f7cc14f3c6c06431ccf574cf1326f98ee6d0027fe836b4a3f9d15539a62ca6eaba674fd860abcbdfa4adf9f773f05d21f49d8341c738678be5f62ebaba9a69524875ff6bbd186b953950c6c4464d5d5e26025b939f5ae8fdcdb9d9f776fb50977a4e8c417ba99713a380ad6bb4f259dc9fdc79afb66eb2e2c77c7f2a570e4754ad0132a72b346381d52785bca9a84252352052e409ce63bb4ef1742add0da620e1373aa17cc855e661a7335f46af86ac8e07bc06fdd98c4324b0c65d482dde9bc2dda8775661fd9a50dc82095dc7fcbb0a14b98d618cd6ff8650aa6249430ce948d8c16dc85468888c5e927d78a8f81668fceb79ca48fab0a277ce33fdd0dc1667f1d2269f1bff20ccfae82e111b3a919bb73c4560080130908024504370e2e01e49d844bb7ba9630ae594c8335a8ed47dd53613b2a36038e3ccbeb46222ee72475796c71860f9880e4d533c2e63b91772b8214b0306544e34a253d90013d4820556dd4c04d60f0809f019a0f3a48e7c537943b7c16d86f2edcd2684b369d498aaf58bc9bcb9edb55a83b08765ad09409afb02341a3e98d224fd5e7901ad87e9f5554a4ef45509050c80c6718ff059b7f9baa06c470629bce6e1ba338ebe4c5f82a2d37a73d6f6b8b72a605cb6f6e8906c3d0c7e0ebb17320c6a799ea80f8e8e85c9008e110ce77bfda22e7189d8febaf47622dcb960c2d5d8e9f52de52cdc010e52303e3021300906052b0e03021a05000414fc77707f3d69b459ecbb93598f205388060383210414c5c98884c74537852ed964c714a10da0bcd024da0203019000	hWOPmRX6jDLmIowjBNTOFkCPHAwBUT5vO01reCllGsiCFQ0qFw==	t	2027-08-21	2026-01-19 17:49:22.345849	2026-01-19 17:49:22.345892	1
\.


--
-- Data for Name: email_config; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.email_config (email_id, email_uuid, user_email, smtp_host, smtp_port, username, password, tls_enabled, ssl_enabled, status_email, enterprise_id, date_created, date_updated, subject_email, body_email) FROM stdin;
1	4d79ebbe-5b2d-40e9-8922-d5b744cb336a	developers@izenshy.com	smtp.gmail.com	587	kingche1234@gmail.com	vrct ppizxtcybeau	t	f	t	2	2025-11-06 15:14:27.587714	2025-11-07 19:27:52.31309	\N	\N
2	a7d7426c-8ebb-4955-b04a-aed04c21e2c0	customers@gilushop.store	smtp.gmail.com	587	gilushopec@gmail.com	eghs worf hcka vwmd	t	f	t	1	2026-01-19 17:50:29.270549	2026-02-26 22:05:51.291091	FACTURA GILU 💖	<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head><body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;"><table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;"><tr><td align="center"><table width="600" cellpadding="0" cellspacing="0" style="background-color: white; border-radius: 15px; overflow: hidden; box-shadow: 0 4px 15px rgba(217, 95, 128, 0.2);"><tr><td style="background: linear-gradient(135deg, #D95F80 0%, #D98BA7 100%); padding: 45px 40px; text-align: center;"><h1 style="color: white; margin: 0; font-size: 34px; text-shadow: 0 2px 4px rgba(0,0,0,0.1);">💞 ¡Hola!</h1><p style="color: #F2BBC9; margin: 12px 0 0 0; font-size: 18px; font-weight: 300;">Gracias por confiar en GILU SHOP</p></td></tr><tr><td style="padding: 45px 40px;"><p style="font-size: 18px; color: #0D0D0D; line-height: 1.8; margin: 0 0 20px 0;">Gracias por tu compra en <strong style="color: #D95F80;">GILU SHOP</strong> — donde la belleza brilla con estilo ✨</p><p style="font-size: 18px; color: #0D0D0D; line-height: 1.8; margin: 0 0 20px 0;">Adjunto encontrarás tu <strong style="color: #D95F80;">factura</strong> con todos los detalles de tu compra.</p><p style="font-size: 18px; color: #0D0D0D; line-height: 1.8; margin: 0 0 35px 0;">Nos encanta que confíes en nosotros para resaltar tu brillo natural 💖</p><div style="background: linear-gradient(135deg, #F2BBC9 0%, #D98BA7 50%); padding: 28px; border-radius: 12px; text-align: center; border-left: 5px solid #D95F80;"><p style="margin: 0; font-size: 17px; color: #0D0D0D; font-weight: 600;">¿Tienes dudas o necesitas ayuda?</p><p style="margin: 8px 0 0 0; font-size: 16px; color: #0D0D0D;">¡Aquí estamos para ti! 💬</p></div></td></tr><tr><td style="background-color: #0D0D0D; padding: 40px 30px; text-align: center;"><table width="100%" cellpadding="8" cellspacing="0"><tr><td style="text-align: center; padding: 8px 0;"><a href="https://instagram.com/gilu.ec" style="display: inline-block; background: linear-gradient(135deg, #D95F80 0%, #D98BA7 100%); color: white; padding: 14px 35px; border-radius: 30px; text-decoration: none; font-weight: bold; font-size: 15px; box-shadow: 0 4px 10px rgba(217, 95, 128, 0.3);">📲 Síguenos en Instagram</a></td></tr><tr><td style="padding-top: 5px; text-align: center; color: #F2BBC9; font-size: 14px;">@gilu.ec</td></tr><tr><td style="text-align: center; padding: 15px 0 8px 0;"><a href="https://gilushop.store" style="display: inline-block; background-color: #B8D5D9; color: #0D0D0D; padding: 14px 35px; border-radius: 30px; text-decoration: none; font-weight: bold; font-size: 15px; box-shadow: 0 4px 10px rgba(184, 213, 217, 0.3);">🌐 Visita nuestra Tienda</a></td></tr><tr><td style="padding-top: 5px; text-align: center; color: #B8D5D9; font-size: 14px;">gilushop.store</td></tr><tr><td style="text-align: center; padding: 15px 0 8px 0;"><a href="https://wa.me/593982901603" style="display: inline-block; background: linear-gradient(135deg, #25D366 0%, #128C7E 100%); color: white; padding: 14px 35px; border-radius: 30px; text-decoration: none; font-weight: bold; font-size: 15px; box-shadow: 0 4px 10px rgba(37, 211, 102, 0.3);">💬 Escríbenos por WhatsApp</a></td></tr><tr><td style="padding-top: 5px; text-align: center; color: #B8D5D9; font-size: 14px;">+593 982901603</td></tr></table></td></tr><tr><td style="padding: 25px; text-align: center; background: linear-gradient(135deg, #F2BBC9 0%, #D98BA7 100%); color: #0D0D0D;"><p style="margin: 0; font-size: 15px; font-weight: 600;">GILU SHOP</p><p style="margin: 5px 0 0 0; font-size: 13px; opacity: 0.8;">Donde la belleza brilla con estilo ✨</p></td></tr></table><table width="600" cellpadding="0" cellspacing="0" style="margin-top: 15px;"><tr><td style="text-align: center; color: #999; font-size: 12px; padding: 10px;">Este correo fue enviado porque realizaste una compra en GILU SHOP</td></tr></table></td></tr></table></body></html>
\.


--
-- Data for Name: emitters; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.emitters (emitter_id, emitter_uuid, razon_social, nombre_comercial, ruc, dir_matriz, cod_estab, pto_emision, emitter_status, date_created, date_updated, ambiente, enterprise_id) FROM stdin;
1	021c9056-285f-486a-8617-c8622d9f88cc	QUINCHE MORAN LUIS ANDRES	IZENSHY	1003866173001	QUITO Y AV. ATAHUALPA	001	001	t	2025-07-30 17:27:29.788276	2025-09-01 12:33:13.91566	1	2
2	e9cf561f-31e8-48b0-ae14-b7fc03cacef9	CABASCANGO ANRANGO GISSEL VANESSA	GILÚ	1716247943001	QUITO Y AV.ATAHUALPA	001	001	t	2026-01-19 17:55:02.181057	2026-02-13 16:02:04.781975	1	1
\.


--
-- Data for Name: enterprises; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.enterprises (enterprise_id, enterprise_uuid, enterprise_name, enterprise_owner_name, enterprise_owner_identification, date_created, date_updated, enterprise_status) FROM stdin;
1	3b54da22-67e0-40e3-97a6-02c17139b3c6	GILU SHOP	GISSEL VANESSA CABASCANGO ANRANGO	1716247943001	2025-09-01 07:22:44.034019	2025-11-05 12:46:31.331785	t
2	988401ca-3f7a-430b-be17-0e1cb0dac701	GILU SHOP STORE	QUINCHE MORAN LUIS ANDRES	1003866173001	2025-09-01 12:32:29.050434	2026-02-03 22:06:14.217055	t
\.


--
-- Data for Name: invoice_detail; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.invoice_detail (detail_id, detail_uuid, quantity, description, total_value, total_value_without_tax, unit_value, unit_value_without_tax, product_tax, stock_product_id, stock_outlet_id, invoice_id, date_created, date_updated) FROM stdin;
1	a1d75731-f9e1-4525-99df-ff8a8ce85a6b	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	\N	2025-11-04 17:21:16.672695	2025-11-04 17:21:16.672738
2	876e9bd7-357d-48e2-9772-3639062a712a	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	2	2025-11-04 17:21:16.696884	2025-11-04 17:21:16.696922
3	b94ae88a-93f0-4ba0-a136-a6d50818b088	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	\N	2025-11-04 17:26:39.067469	2025-11-04 17:26:39.067551
4	5a9f8d7f-be90-4f0d-ba00-d4144816da62	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	3	2025-11-04 17:26:39.084413	2025-11-04 17:26:39.084485
5	e38d58d1-d452-40dc-9cdb-71c95f8c6e69	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	\N	\N	\N	2025-11-04 17:29:50.340152	2025-11-04 17:29:50.34018
6	74ff8c62-e6e8-4f63-9175-17d609b6bb18	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	\N	\N	4	2025-11-04 17:29:50.350115	2025-11-04 17:29:50.350143
7	7220522b-fb15-43fb-a35c-a9c2ea103ff0	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	\N	2025-11-05 16:48:07.350369	2025-11-05 16:48:07.350409
8	51ab4139-4eca-40cb-aa35-473cfb18dcb4	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	5	2025-11-05 16:48:07.381982	2025-11-05 16:48:07.382033
9	e7b2bd08-307f-413f-bcaa-5a908bfe146c	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	\N	\N	\N	2025-11-05 16:50:43.475471	2025-11-05 16:50:43.475532
10	05b188cd-8aad-4b79-9a62-e8fd1f083833	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	\N	2025-11-05 16:50:43.479729	2025-11-05 16:50:43.479847
11	17da266e-030a-4265-a844-5c56c0ad7334	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	\N	\N	6	2025-11-05 16:50:43.500331	2025-11-05 16:50:43.500395
12	c63e9a14-b920-40d2-9227-77ada1e63751	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	6	2025-11-05 16:50:43.514287	2025-11-05 16:50:43.514353
13	855cfab2-1a74-44a4-8570-94b0e1e4a129	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	\N	\N	\N	2025-11-05 20:28:34.808195	2025-11-05 20:28:34.808219
14	34a59262-cf1b-4d7c-8377-bd25f97b7e3a	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	\N	\N	\N	2025-11-05 20:28:34.810245	2025-11-05 20:28:34.810275
15	7ae11222-4148-4262-8390-a2c23ca15458	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	\N	2025-11-05 21:52:39.530117	2025-11-05 21:52:39.530154
16	7ad89b19-422c-427c-8da7-05e1c5e765e9	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	\N	\N	\N	2025-11-05 21:52:39.532952	2025-11-05 21:52:39.532991
17	4bad3e41-211d-4b44-9cd1-1f38d4d71cae	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	\N	\N	\N	2025-11-05 22:22:50.894647	2025-11-05 22:22:50.894668
18	d86ececf-a2e6-4b8e-b259-38f3cd984bba	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	\N	\N	10	2025-11-05 23:10:58.933971	2025-11-05 23:10:58.934
19	e6cb74f2-7179-4845-b7bb-c98aad318943	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	\N	\N	11	2025-11-05 23:21:18.616998	2025-11-05 23:21:18.617031
20	b3c147e9-3ea5-4866-b2f2-f73d19cb2068	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	\N	\N	12	2025-11-05 23:24:16.644857	2025-11-05 23:24:16.644878
21	d2e31a50-4238-403b-a7b2-8028f5ada207	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	\N	\N	12	2025-11-05 23:24:16.646082	2025-11-05 23:24:16.646097
200	7324cf68-8327-40d0-b504-dea05e9096e7	1	VICTORIA SECRET SPLASH	17.00	17.00	17.00	17.00	0.00	227	1	153	2026-02-03 21:52:48.695032	2026-02-03 21:52:48.695056
201	3c79663b-5d0e-46e9-a479-3532f64b5cb7	5	SPLASH VICTORIA SECRET	85.00	85.00	17.00	17.00	0.00	228	1	154	2026-02-03 22:13:54.37454	2026-02-03 22:13:54.374575
202	67302147-82de-445b-9172-06fce8f0021d	1	CREMA VICTORIA SECRET	17.00	17.00	17.00	17.00	0.00	229	1	154	2026-02-03 22:13:54.376499	2026-02-03 22:13:54.376545
203	f814ef98-7090-466d-a354-36f52a0f3754	2	E.L.F DELINEADORES	10.70	10.70	5.35	5.35	0.00	230	1	154	2026-02-03 22:13:54.378438	2026-02-03 22:13:54.378456
204	aea15118-9003-4176-8e0a-a3aa44ecea50	2	KIKO MILANO 3D HYDRA GLOSS	28.90	28.90	14.45	14.45	0.00	231	1	154	2026-02-03 22:13:54.380034	2026-02-03 22:13:54.38005
205	e31a8f7d-8795-43ec-beaf-4ad3d3880e46	2	GOT2B GLUE MINI	14.30	14.30	7.15	7.15	1.87	20	1	154	2026-02-03 22:13:54.38124	2026-02-03 22:13:54.381256
206	40916b51-5770-43e1-8e7a-61db1b242295	1	E.L.F TINT BULSH	11.70	11.70	11.70	11.70	1.53	232	1	154	2026-02-03 22:13:54.382616	2026-02-03 22:13:54.382639
207	153bf9ff-1a3b-47fd-9a34-a76f196df4f8	1	FIJADOR LOREAL	20.00	20.00	20.00	20.00	2.61	233	1	154	2026-02-03 22:13:54.383763	2026-02-03 22:13:54.383778
208	9a248c8e-91f2-4ef5-bbb9-d0ca0cb5417c	1	MAYBELLINE BASE MATTE FIT ME 322	15.70	15.70	15.70	15.70	2.05	234	1	154	2026-02-03 22:13:54.384725	2026-02-03 22:13:54.384738
209	cfdcedcd-a841-4dee-bcf8-8f63fe6b8b58	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 120	18.00	18.00	18.00	18.00	2.35	93	1	154	2026-02-03 22:13:54.385863	2026-02-03 22:13:54.385878
210	d458c76f-9a62-4680-9945-10df8e085f8e	1	LOREAL INFALLIBLE POWDER 120	16.15	16.15	16.15	16.15	2.11	235	1	154	2026-02-03 22:13:54.387243	2026-02-03 22:13:54.38727
211	a89384d8-6ac4-49f6-9951-32d70610cb71	1	LOREAL INFALLIBLE FRESH WEAR 410	17.85	17.85	17.85	17.85	2.33	236	1	154	2026-02-03 22:13:54.388594	2026-02-03 22:13:54.38861
212	b0227f30-ca29-4c4b-9937-965471ff6ccd	1	ELF HYDRATING CAMO CONCEALER MEDIUM NEUTRAL	11.70	11.70	11.70	11.70	1.53	145	1	154	2026-02-03 22:13:54.389491	2026-02-03 22:13:54.389505
215	745ecca2-1450-48af-adcf-bcf94ad38964	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	1	157	2026-02-07 17:06:25.030887	2026-02-07 17:06:25.030918
217	dddee6af-a9c2-4fef-ad07-e6d2be55a949	1	ELF LIP OIL CANDY CODED	13.00	13.00	13.00	13.00	1.70	35	2	159	2026-02-12 17:47:06.42831	2026-02-12 17:47:06.428338
221	906f104b-3997-4960-9150-51776ebcb6c2	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 120	15.52	15.52	15.52	15.52	2.33	93	2	163	2026-02-13 13:41:48.709615	2026-02-13 13:41:48.70964
222	dbbeaf75-901a-4b90-8cd8-eba341b9ccd1	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 312	15.52	15.52	15.52	15.52	2.33	238	2	163	2026-02-13 13:41:48.713964	2026-02-13 13:41:48.713988
223	941c4549-76da-4810-a277-3a4eef913236	1	MAYBELLINE SUPER STAY TEDDY TINT 55 KNEEHIGH	11.83	11.83	11.83	11.83	1.77	239	2	163	2026-02-13 13:41:48.716228	2026-02-13 13:41:48.716263
224	5c15f3c6-e59f-4199-aec5-e9a075910336	1	ELF HALO GLOW LIQUID FILTER 2 FAIR LIGHT	14.04	14.04	14.04	14.04	2.11	130	2	163	2026-02-13 13:41:48.718555	2026-02-13 13:41:48.718581
225	83abd3f8-0dcc-4139-ae47-dbcaa2b20726	1	VICTORIA SECRET SPLASH SHIMMER BARE VANILLA	14.78	14.78	14.78	14.78	2.22	240	2	163	2026-02-13 13:41:48.720968	2026-02-13 13:41:48.721003
226	d504960c-6e31-4b49-9cc9-4a1c0bdc5485	1	VICTORIA SECRET CREMA SHIMMER BARE VANILLA	14.78	14.78	14.78	14.78	2.22	241	2	163	2026-02-13 13:41:48.723825	2026-02-13 13:41:48.72385
227	f5c72f31-4ea9-4475-a184-559925f5ad3e	1	MAYBELLINE FIT ME BASE 125	13.65	13.65	13.65	13.65	2.05	79	2	163	2026-02-13 13:41:48.725896	2026-02-13 13:41:48.725928
228	4c959b78-d9d3-4573-9f2e-47b921c3a5ca	1	MAYBELLINE FIT ME BASE 220	16.52	16.52	16.52	16.52	2.48	77	2	164	2026-02-13 13:47:57.133324	2026-02-13 13:47:57.133411
240	94a451bb-7b3d-4abd-8f1b-468bf6b25e14	1	MADAGASCAR CENTELLA TONE BRIGHTENING AMPOULE	19.80	19.80	19.80	19.80	0.00	242	2	173	2026-02-14 13:27:00.179978	2026-02-14 13:27:00.180035
241	61a937c0-e50c-4244-bbf9-fd3f34cb78ff	1	ELF LIP OIL CANDY CODED	12.15	12.15	12.15	12.15	1.58	35	2	173	2026-02-14 13:27:00.195025	2026-02-14 13:27:00.195084
242	900291e1-aaf2-4857-9af2-6e315f4cd993	1	ELF HYDRATING CAMO CONCEALER MEDIUM WARM	12.15	12.15	12.15	12.15	1.58	141	2	173	2026-02-14 13:27:00.202916	2026-02-14 13:27:00.203007
243	65dc24ea-c1b9-4c6d-8ae5-61b69a637fbf	1	LOREAL TRUE MATCHCH 2-3	16.80	16.80	16.80	16.80	2.19	243	2	173	2026-02-14 13:27:00.211864	2026-02-14 13:27:00.21192
244	4be6f9d8-ec61-4766-bc89-c24e1ee97c01	1	MAXFINE LIP OIL 06	2.87	2.87	2.87	2.87	0.43	73	2	175	2026-02-25 13:47:08.176786	2026-02-25 13:47:08.176822
245	cc51b829-abe0-4539-ba4f-302f1c050e52	1	ELF LIP OIL CANDY CODED	11.30	11.30	11.30	11.30	1.70	35	2	176	2026-02-25 13:53:02.913403	2026-02-25 13:53:02.913459
246	096817ca-2e83-422d-90ac-32709c446d1a	1	ELF LIP OIL CANDY CODED	11.30	11.30	11.30	11.30	1.70	35	2	177	2026-02-26 08:30:30.691597	2026-02-26 08:30:30.69171
247	5f282869-c4ac-4404-b34b-46a4038fff03	1	MAYBELLINE LIFTER GLOSS AMBER	13.91	13.91	13.91	13.91	2.09	43	2	178	2026-02-26 17:10:24.738075	2026-02-26 17:10:24.738095
213	d311b0ae-5635-4609-a4cf-61858b1448c0	5	GOT2B GLUE MINI	40.00	40.00	8.00	8.00	5.22	20	1	155	2026-02-03 23:11:39.398524	2026-02-03 23:11:39.398552
214	badd82c7-2a36-4d07-a97d-48d923b022b9	4	GOT2B GLUE MINI	32.00	32.00	8.00	8.00	4.17	20	1	156	2026-02-03 23:12:17.0168	2026-02-03 23:12:17.016821
216	d9af95eb-8d6d-41d4-95ba-aeabc5574ab4	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	0.00	178	1	129	2026-02-07 17:50:34.556392	2026-02-07 17:50:34.556464
218	9d635f8b-4bd2-4364-9b36-8ce12f465c99	1	ELF LIP OIL CANDY CODED	13.00	13.00	13.00	13.00	1.70	35	2	160	2026-02-12 18:00:15.803518	2026-02-12 18:00:15.803547
229	ead36aeb-d32e-4ac5-96dc-bd89c8ca497b	1	VICTORIA SECRET SPLASH SHIMMER BARE VANILLA	14.78	14.78	14.78	14.78	2.22	240	2	166	2026-02-13 14:23:08.203818	2026-02-13 14:23:08.203837
230	2e438e18-92c2-4cd6-a51e-804ac44629b4	1	VICTORIA SECRET CREMA SHIMMER BARE VANILLA	14.78	14.78	14.78	14.78	2.22	241	2	166	2026-02-13 14:23:08.206643	2026-02-13 14:23:08.206661
231	53cd1389-4d3e-4fe6-9921-09ccb8e1b822	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 312	15.52	15.52	15.52	15.52	2.33	238	2	166	2026-02-13 14:23:08.208192	2026-02-13 14:23:08.208208
232	5345a9e6-584e-48e3-bf6c-e12967d81e87	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 120	15.52	15.52	15.52	15.52	2.33	93	2	166	2026-02-13 14:23:08.209914	2026-02-13 14:23:08.209929
233	06626fce-5396-4dc9-9ccf-3827e66c5690	1	MAYBELLINE SUPER STAY TEDDY TINT 55 KNEEHIGH	11.83	11.83	11.83	11.83	1.77	239	2	166	2026-02-13 14:23:08.211402	2026-02-13 14:23:08.211418
234	b0ece3ce-44d8-47f0-b58c-0e29b65aea4b	1	MAYBELLINE FIT ME BASE 125	13.65	13.65	13.65	13.65	2.05	79	2	166	2026-02-13 14:23:08.214118	2026-02-13 14:23:08.214135
235	128689cd-b06e-420f-b0b9-b246f9699b4d	1	ELF HALO GLOW LIQUID FILTER 2 FAIR LIGHT	14.04	14.04	14.04	14.04	2.11	130	2	166	2026-02-13 14:23:08.215732	2026-02-13 14:23:08.215748
248	94fb0312-a205-4749-80e2-552bd14139f7	1	ELF LIP OIL SUPER NEUTRAL	11.30	11.30	11.30	11.30	1.70	36	2	179	2026-02-26 17:45:14.546826	2026-02-26 17:45:14.546849
219	52ef3c76-3466-4fe4-8903-4585ec6cf5c1	1	NYX FAT OIL NEWSPEED	14.00	14.00	14.00	14.00	1.83	41	2	161	2026-02-12 18:22:41.518696	2026-02-12 18:22:41.51873
236	70192c56-ba8e-4e59-bf11-96699dc431fb	1	MAYBELLINE LIFTER GLOSS AMBER	13.91	13.91	13.91	13.91	2.09	43	2	168	2026-02-13 16:08:27.004357	2026-02-13 16:08:27.004378
249	34652300-0a4c-420f-b695-b67f1e5584db	1	ELF BASE HALO GLOW	17.39	17.39	17.39	17.39	2.61	4	2	180	2026-02-26 17:52:49.007209	2026-02-26 17:52:49.00724
220	498032cd-3128-4f60-9a80-601a30095c00	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 120	17.85	17.85	17.85	17.85	2.33	93	2	162	2026-02-12 18:59:38.290061	2026-02-12 18:59:38.29012
237	5fc7aa4a-f8a9-4d02-ab20-3e264c642930	1	MAYBELLINE LIFTER GLOSS AMBER	13.91	13.91	13.91	13.91	2.09	43	2	170	2026-02-13 16:58:01.345177	2026-02-13 16:58:01.345198
238	6b0f68e1-c462-4a71-b119-3307cd10c888	1	ELF LIP OIL JAM SESSION	11.30	11.30	11.30	11.30	1.70	23	2	171	2026-02-13 17:10:02.907216	2026-02-13 17:10:02.907247
250	6cdf4e4e-8fac-42da-908d-68a5756f16c4	1	MAYBELLINE LIFTER GLOSS AMBER	13.91	13.91	13.91	13.91	2.09	43	2	181	2026-02-26 18:01:45.910926	2026-02-26 18:01:45.910984
159	d950f4b7-950e-4db5-ac5f-f383f60dcd57	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	109	2025-12-12 19:16:26.569595	2026-01-19 17:55:54.72784
164	a3155270-7e55-4f95-a7a5-28261b5365f9	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	114	2025-12-27 14:04:55.256741	2026-01-19 17:55:54.72784
165	68eb583c-4911-4de3-bee6-3090db329634	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	115	2025-12-27 14:06:24.985612	2026-01-19 17:55:54.72784
177	b1f9d7b8-0067-4ebf-af10-ce6559357fdf	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	128	2026-01-03 18:41:33.554209	2026-01-19 17:55:54.72784
181	da0a0b93-c437-4b20-8a89-34fdfd80f3c1	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	132	2026-01-09 12:56:54.195571	2026-01-19 17:55:54.72784
194	16fadd87-0194-4027-a338-31b8eddd385b	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	145	2026-01-17 18:09:43.448488	2026-01-19 17:55:54.72784
195	d34e2a35-b306-4563-a853-76775ace0e19	1	MAYBELLINE FIT ME BASE 220	19.00	19.00	19.00	19.00	2.48	77	2	146	2026-01-17 18:10:08.164955	2026-01-19 17:55:54.72784
170	6bd7b8c1-5681-40e8-b415-e07809ed4004	1	MAYBELLINE FIT ME BASE 125	19.00	19.00	19.00	19.00	2.48	79	2	120	2025-12-27 14:39:47.931095	2026-01-19 17:55:54.72784
59	47d17417-035a-40c5-b348-85a6080aedc1	3	Camiseta Azul	22.50	22.50	7.50	7.50	2.93	18	2	46	2025-11-14 17:35:07.708059	2026-01-19 17:55:54.72784
61	fa2f5c16-53f5-44fa-bdcf-ce59cf1bc308	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	47	2025-11-14 17:36:53.881019	2026-01-19 17:55:54.72784
62	23cbbe19-2603-4d00-885c-347cd22b090b	3	Camiseta Azul	22.50	22.50	7.50	7.50	2.93	18	2	48	2025-11-14 17:39:54.136138	2026-01-19 17:55:54.72784
63	f8fa414e-8420-4da8-9f03-5fe180778829	3	Camiseta Azul	22.50	22.50	7.50	7.50	2.93	18	2	49	2025-11-14 17:41:29.929571	2026-01-19 17:55:54.72784
64	47ac846c-b06c-4dcb-9838-ff5e0b5fce46	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	50	2025-11-14 17:44:30.019584	2026-01-19 17:55:54.72784
65	63cc7941-4591-4ab3-9b80-197c2a7557b1	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	51	2025-11-14 17:50:16.400936	2026-01-19 17:55:54.72784
66	47671fc0-0dcb-4888-bb74-9972eec48615	4	Camiseta Azul	30.00	30.00	7.50	7.50	3.91	18	2	52	2025-11-14 17:53:32.246439	2026-01-19 17:55:54.72784
67	f463ee3e-5904-4fb1-bc01-a0793122f819	4	Camiseta Azul	30.00	30.00	7.50	7.50	3.91	18	2	53	2025-11-14 17:55:18.891848	2026-01-19 17:55:54.72784
68	1dadade9-fea0-4bf8-9d61-4f5e9894c8c2	4	Camiseta Azul	30.00	30.00	7.50	7.50	3.91	18	2	54	2025-11-14 17:55:32.512013	2026-01-19 17:55:54.72784
69	e7ba200e-f3c0-4087-a838-0e1e5b715178	5	Camiseta Azul	37.50	37.50	7.50	7.50	4.89	18	2	55	2025-11-14 17:58:15.914266	2026-01-19 17:55:54.72784
70	d5e89393-58d6-4453-bd96-2ea22622201c	2	Camiseta Azul	15.00	15.00	7.50	7.50	1.96	18	2	56	2025-11-14 17:59:47.688712	2026-01-19 17:55:54.72784
71	9c38667f-8f68-4fb7-817b-b6a32bdeff0d	2	Camiseta Azul	15.00	15.00	7.50	7.50	1.96	18	2	57	2025-11-14 18:03:18.617931	2026-01-19 17:55:54.72784
72	cec40b81-bc64-4b04-9465-f5651709ed14	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	58	2025-11-14 18:06:04.398475	2026-01-19 17:55:54.72784
73	1b16c260-de2f-48a2-9e06-aea10e7195cb	4	Camiseta Azul	30.00	30.00	7.50	7.50	3.91	18	2	59	2025-11-14 18:08:11.616742	2026-01-19 17:55:54.72784
74	9b9370f1-7ff6-4162-a8c2-d117d2b2520e	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	60	2025-11-14 18:11:19.493256	2026-01-19 17:55:54.72784
75	0530671f-8b04-400e-baa3-ff92e20f58c3	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	61	2025-11-14 18:14:53.911193	2026-01-19 17:55:54.72784
78	33dcbefc-7f16-4460-bca8-d8690d5c9f2d	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	62	2025-11-14 18:17:12.068324	2026-01-19 17:55:54.72784
82	a0a95576-d008-4e60-afc8-e0e6ef96a9a1	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	63	2025-11-14 18:18:01.796862	2026-01-19 17:55:54.72784
88	efc20ab4-ab61-43de-88b8-fd1addc70946	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	64	2025-11-14 18:19:24.294195	2026-01-19 17:55:54.72784
90	b58621e9-f7e4-4ff1-a5d4-6ee867c68c0e	5	Camiseta Azul	37.50	37.50	7.50	7.50	4.89	18	2	65	2025-11-14 18:20:54.578042	2026-01-19 17:55:54.72784
91	edeb103d-9c01-4c9c-8f73-674d731e7edc	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	66	2025-11-14 18:22:51.655958	2026-01-19 17:55:54.72784
95	67927626-45c9-426b-8fa9-29a3308fbe10	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	67	2025-11-14 18:23:16.141633	2026-01-19 17:55:54.72784
97	5ba5e763-1ac2-4c40-b038-d307bebf9f81	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	68	2025-11-14 18:24:34.26189	2026-01-19 17:55:54.72784
99	de3c5573-565f-43c1-b059-4ae7795e01e4	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	69	2025-11-14 18:25:48.771155	2026-01-19 17:55:54.72784
100	1e05d881-1e49-42e9-b85a-f1597d156ed1	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	70	2025-11-14 18:26:31.269952	2026-01-19 17:55:54.72784
104	3ae5abfb-1a9b-4bcf-87f0-6a88c1457ec1	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	71	2025-11-14 18:27:03.951296	2026-01-19 17:55:54.72784
105	b9dc5c89-f4b1-47f2-8812-802907b904b1	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	72	2025-11-14 18:28:14.923141	2026-01-19 17:55:54.72784
109	c43a8133-db51-41dd-84c4-e6e73d3145eb	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	73	2025-11-14 18:29:24.94196	2026-01-19 17:55:54.72784
114	c84eb1e1-a89e-4e7b-8501-890b8a1c1591	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	74	2025-11-14 18:30:21.427565	2026-01-19 17:55:54.72784
119	a4916987-59ad-4284-8e1c-4e9bca8bc32e	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	75	2025-11-14 18:35:14.919534	2026-01-19 17:55:54.72784
122	0a1ea217-9ec0-4c41-98ba-2731bc82de35	1	Camiseta Azul	7.50	7.50	7.50	7.50	0.98	18	2	76	2025-11-14 18:38:04.473687	2026-01-19 17:55:54.72784
60	629f23c4-f57c-45eb-b355-43a96c177a06	2	Pantalón Negro	30.00	30.00	15.00	15.00	0.00	19	2	47	2025-11-14 17:36:53.875614	2026-01-19 17:55:54.72784
81	a5a23dce-7808-4c49-837c-eeb368f5af32	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	62	2025-11-14 18:17:12.081575	2026-01-19 17:55:54.72784
85	6db3fe05-62e5-446c-9d2a-a8a3b9d709c2	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	63	2025-11-14 18:18:01.803138	2026-01-19 17:55:54.72784
89	18952d46-4abc-4477-8516-38d591232f90	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	64	2025-11-14 18:19:24.297256	2026-01-19 17:55:54.72784
92	46263ff5-2864-4518-91af-f4cb7c45b6ef	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	66	2025-11-14 18:22:51.66148	2026-01-19 17:55:54.72784
96	8231625b-bb58-434b-acdd-4a9492ab91bc	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	67	2025-11-14 18:23:16.144645	2026-01-19 17:55:54.72784
103	e7431ea4-7354-45ab-a46f-0a3d10027ba1	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	70	2025-11-14 18:26:31.276897	2026-01-19 17:55:54.72784
106	672ff2ae-958e-49ca-ac60-bbc2b31d5940	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	72	2025-11-14 18:28:14.924971	2026-01-19 17:55:54.72784
110	e7c2eb46-b09e-49f2-9629-154e7bdc851a	1	Pantalón Negro	15.00	15.00	15.00	15.00	0.00	19	2	73	2025-11-14 18:29:24.943663	2026-01-19 17:55:54.72784
113	cb10cc23-3064-4750-a01c-1b0d3e741872	1	PANTALON NEGRO	15.00	15.00	15.00	15.00	0.00	19	2	74	2025-11-14 18:30:21.424482	2026-01-19 17:55:54.72784
120	c25d6186-ab93-4744-81c6-ea4b92e07987	1	PANTALON NEGRO	15.00	15.00	15.00	15.00	0.00	19	2	75	2025-11-14 18:35:14.922113	2026-01-19 17:55:54.72784
121	44389eb6-8afa-46c3-8ca1-a34064b1a068	1	PANTALON NEGRO	15.00	15.00	15.00	15.00	0.00	19	2	76	2025-11-14 18:38:04.469149	2026-01-19 17:55:54.72784
28	0f80c05f-7ff7-4ae0-83bc-dc11096d8f6d	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	18	2025-11-11 15:06:12.941961	2026-01-19 17:55:54.72784
31	2b4c8471-c02b-467b-bb19-31e02f56a893	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	20	2025-11-11 15:26:30.839564	2026-01-19 17:55:54.72784
50	4fd031ae-6b1d-49a8-ac7f-ea68d6d45ed9	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	4	2	39	2025-11-14 17:04:03.00907	2026-01-19 17:55:54.72784
53	26ee27f4-ed9b-4103-af9d-21b86a976bab	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	4	2	41	2025-11-14 17:13:05.573665	2026-01-19 17:55:54.72784
56	4cf0e96f-a54b-4ad7-8ed0-4b6ea37f8997	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	43	2025-11-14 17:24:49.871469	2026-01-19 17:55:54.72784
57	e66b46a5-18ca-48a2-a1a1-9c765c2fa579	3	ELF BASE HALO GLOW	60.00	60.00	20.00	20.00	7.83	4	2	44	2025-11-14 17:29:21.734043	2026-01-19 17:55:54.72784
58	33989269-4ffc-468c-a2c6-a61aa466e4ea	3	ELF BASE HALO GLOW	60.00	60.00	20.00	20.00	7.83	4	2	45	2025-11-14 17:33:16.255305	2026-01-19 17:55:54.72784
76	4eb13053-652f-48b4-a5fa-0a2dcd542a6e	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	61	2025-11-14 18:14:53.915164	2026-01-19 17:55:54.72784
79	b3109d2b-5d93-420d-ac46-d56da4321777	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	62	2025-11-14 18:17:12.074606	2026-01-19 17:55:54.72784
83	4dcac7d2-3071-4f94-980c-2b75ba60c2f9	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	63	2025-11-14 18:18:01.799489	2026-01-19 17:55:54.72784
87	312944aa-e198-4ad5-a36a-057f02252098	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	64	2025-11-14 18:19:24.290812	2026-01-19 17:55:54.72784
93	1186c3cc-a39f-4878-a4ab-70b896d63fa8	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	66	2025-11-14 18:22:51.665296	2026-01-19 17:55:54.72784
101	f5bfbb77-3c5a-4767-b329-013d8153f4a6	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	70	2025-11-14 18:26:31.272208	2026-01-19 17:55:54.72784
108	bf8d9c74-82c9-4e5b-9120-4ec2f9b077d6	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	72	2025-11-14 18:28:14.928244	2026-01-19 17:55:54.72784
112	e8453118-2cba-46b9-a307-75fc3a1beea6	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	73	2025-11-14 18:29:24.947233	2026-01-19 17:55:54.72784
115	02f93f26-ea33-4a3a-847a-952124dbcecb	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	74	2025-11-14 18:30:21.429575	2026-01-19 17:55:54.72784
118	87d05e06-fc5c-496c-b8d7-f6a67eec7665	2	ELF BASE HALO GLOW	40.00	40.00	20.00	20.00	5.22	4	2	75	2025-11-14 18:35:14.916389	2026-01-19 17:55:54.72784
123	6ec7c15f-aa00-4af5-a9c4-fa3295ed0bd6	1	ELF BASE HALO GLOW	20.00	20.00	20.00	20.00	2.61	4	2	76	2025-11-14 18:38:04.476469	2026-01-19 17:55:54.72784
22	156b1594-e633-45cc-b88f-d2e774f73636	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	13	2025-11-06 14:07:23.312925	2026-01-19 17:55:54.72784
23	37b49fb2-cb04-4434-a544-0e02c3322739	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	14	2025-11-08 17:09:14.697016	2026-01-19 17:55:54.72784
24	fc30b30f-4d4b-4e23-b32c-64d7999a0c22	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	15	2025-11-08 17:16:30.22819	2026-01-19 17:55:54.72784
25	a563e57b-e70a-4b19-8cb1-a217f8c61049	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	16	2025-11-08 17:21:21.313357	2026-01-19 17:55:54.72784
26	cf9a5cc3-b31d-439b-8b5d-2ed10af2687f	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	17	2025-11-08 17:24:09.471861	2026-01-19 17:55:54.72784
27	5f5fa1e6-4d79-465a-84ee-ac1299993d99	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	18	2025-11-11 15:06:12.934844	2026-01-19 17:55:54.72784
29	1c3ff6f0-edc9-4ca1-8137-ac18986df0b6	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	19	2025-11-11 15:10:29.299585	2026-01-19 17:55:54.72784
30	163f3996-9252-4ab2-b3d1-43f7f523f2bc	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	20	2025-11-11 15:26:30.835333	2026-01-19 17:55:54.72784
32	8442b924-cf30-4da0-a955-ce2be2e2e713	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	21	2025-11-11 16:25:47.439085	2026-01-19 17:55:54.72784
33	7fd81ceb-f80b-4218-969e-0438353bce16	3	MAYBELLINE - LUMIMATTE	39.00	39.00	13.00	13.00	5.09	1	2	22	2025-11-11 16:29:28.057213	2026-01-19 17:55:54.72784
34	40f6f952-b604-4e75-a1fb-65adf61c2bc1	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	23	2025-11-11 16:32:17.578938	2026-01-19 17:55:54.72784
35	50ac9e6d-616e-45dd-8a91-2e47708c2c2e	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	24	2025-11-11 16:36:14.386683	2026-01-19 17:55:54.72784
36	0bdece14-bfac-41ec-acd6-ab2d56ca10fb	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	25	2025-11-11 16:45:20.184419	2026-01-19 17:55:54.72784
37	70b99f28-b307-481c-8069-773e83ed6112	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	26	2025-11-11 16:57:21.329386	2026-01-19 17:55:54.72784
38	9965a852-85ba-4df9-bdb4-a39bf36da850	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	27	2025-11-11 17:16:15.662655	2026-01-19 17:55:54.72784
39	29b3b84c-f32c-4856-8c08-4cbac824e95e	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	28	2025-11-11 17:23:28.826718	2026-01-19 17:55:54.72784
40	ff569182-9977-4c93-a7ec-160064b2600c	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	29	2025-11-11 17:35:07.982857	2026-01-19 17:55:54.72784
41	272a614c-e0a6-47cf-a9ef-4dbd7860e7a6	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	30	2025-11-11 17:36:35.155912	2026-01-19 17:55:54.72784
42	2e332e30-0b76-4d30-aa34-2e1b033c7a87	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	31	2025-11-11 17:39:30.789554	2026-01-19 17:55:54.72784
43	a58cf2d4-6395-4f02-8fbc-9bdecb8cb60d	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	32	2025-11-11 17:43:25.223782	2026-01-19 17:55:54.72784
44	a2f92bd3-5a69-44d2-aa00-277e76db4281	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	33	2025-11-11 18:06:56.217085	2026-01-19 17:55:54.72784
45	d4129130-2eed-4fb3-8ebb-e33f446c1e92	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	34	2025-11-14 15:40:55.26943	2026-01-19 17:55:54.72784
46	faf14487-27cc-422a-a7aa-dcb17ded9bae	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	35	2025-11-14 16:46:51.247357	2026-01-19 17:55:54.72784
47	2c4442c3-28ea-4401-aa9c-c7b40e0d7a24	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	36	2025-11-14 16:50:00.871479	2026-01-19 17:55:54.72784
48	907d17cd-a9ef-4963-9e1f-88eb78ff3556	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	37	2025-11-14 16:58:41.355642	2026-01-19 17:55:54.72784
49	b2f43243-6c31-4667-84de-eaf4282888fb	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	38	2025-11-14 17:01:38.400627	2026-01-19 17:55:54.72784
51	e51c6887-aa6e-4722-a5ca-96014ff1363d	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	40	2025-11-14 17:08:23.524922	2026-01-19 17:55:54.72784
52	ecb087d0-a9c6-41c1-8715-3a777b30cd15	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	41	2025-11-14 17:13:05.565133	2026-01-19 17:55:54.72784
54	19d5e77b-7d7f-4ade-892e-fafea6b75ad0	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	42	2025-11-14 17:20:04.879177	2026-01-19 17:55:54.72784
55	19b5cfde-be18-480c-aeb4-2d00ab9b862a	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	43	2025-11-14 17:24:49.866728	2026-01-19 17:55:54.72784
77	5bc2ac02-986d-4a9e-aadb-4148ba942b63	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	61	2025-11-14 18:14:53.917597	2026-01-19 17:55:54.72784
80	04d759eb-bf4b-4f87-b002-372cb0c2cff7	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	62	2025-11-14 18:17:12.078183	2026-01-19 17:55:54.72784
84	1cd412c1-b189-47f7-a7e2-b0ef80705c9b	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	63	2025-11-14 18:18:01.801555	2026-01-19 17:55:54.72784
86	8eb9604d-fe00-4458-9c20-a68527eeec29	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	64	2025-11-14 18:19:24.285582	2026-01-19 17:55:54.72784
94	dba2e32c-f3b1-4e44-a754-4c492696d8a4	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	66	2025-11-14 18:22:51.669326	2026-01-19 17:55:54.72784
98	ea6cd650-8345-4ad2-b1ac-ffff64564ae1	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	68	2025-11-14 18:24:34.266688	2026-01-19 17:55:54.72784
102	2a9bdd16-9fb5-4362-9bd3-a47e8931a9cb	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	70	2025-11-14 18:26:31.274592	2026-01-19 17:55:54.72784
107	f0c1ef9c-2052-4d53-9a12-311a74c0e759	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	72	2025-11-14 18:28:14.926593	2026-01-19 17:55:54.72784
111	c6028ce0-2097-4878-8619-b878cf484312	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	73	2025-11-14 18:29:24.945247	2026-01-19 17:55:54.72784
116	7aba261a-2656-49a3-872f-fe093dfbdac9	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	74	2025-11-14 18:30:21.431625	2026-01-19 17:55:54.72784
117	a5acf346-3845-4300-80d4-81a796d46765	2	MAYBELLINE - LUMIMATTE	26.00	26.00	13.00	13.00	3.39	1	2	75	2025-11-14 18:35:14.910978	2026-01-19 17:55:54.72784
124	88094a56-42c7-4b85-ad6e-70dfc1ff2fc5	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	76	2025-11-14 18:38:04.478555	2026-01-19 17:55:54.72784
166	3e2adc28-ede8-46cf-bba7-1735c698f5cb	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	116	2025-12-27 14:20:56.429315	2026-01-19 17:55:54.72784
199	bc0f3e5e-5b65-435a-b057-f5c01cbd3220	1	MAYBELLINE - LUMIMATTE	13.00	13.00	13.00	13.00	1.70	1	2	150	2026-01-17 18:26:27.696196	2026-01-19 17:55:54.72784
127	e7aafcac-d399-4e44-8e93-ec8477eb8193	1	GOT2B GLUE MINI	8.00	8.00	8.00	8.00	1.04	20	2	78	2025-11-14 18:51:14.973058	2026-01-19 17:55:54.72784
179	e9ababce-1425-4f55-83f1-0a5c7fb3800d	1	ELF LIP OIL JELLY POP	13.00	13.00	13.00	13.00	1.70	27	2	130	2026-01-06 00:03:09.351197	2026-01-19 17:55:54.72784
180	94251283-1fd2-4ab2-bb6b-0cae59cdc72e	1	ELF LIP OIL RED DELICIOUS	13.00	13.00	13.00	13.00	1.70	32	2	131	2026-01-06 00:06:37.15007	2026-01-19 17:55:54.72784
128	b764d253-a013-4ac7-85c8-e4eeae939146	2	ELF LIP OIL CANDY CODED	26.00	26.00	13.00	13.00	3.39	35	2	79	2025-11-24 17:54:40.240422	2026-01-19 17:55:54.72784
129	ec70bc1d-4758-40cd-b361-7f4e28cede59	2	ELF LIP OIL CANDY CODED	26.00	26.00	13.00	13.00	3.39	35	2	80	2025-11-24 17:57:43.190691	2026-01-19 17:55:54.72784
153	2f02326e-515f-490e-8980-1c060d2a9660	1	ELF LIP OIL CANDY CODED	13.00	13.00	13.00	13.00	1.70	35	2	104	2025-12-12 18:20:04.0155	2026-01-19 17:55:54.72784
187	b426ccce-b1fa-486e-9256-3eb9cdeeebdd	1	ELF LIP OIL CANDY CODED	13.00	13.00	13.00	13.00	1.70	35	2	138	2026-01-09 18:13:20.853455	2026-01-19 17:55:54.72784
190	53eec343-6de3-47e9-abf0-8707c534f654	1	ELF LIP OIL CANDY CODED	13.00	13.00	13.00	13.00	1.70	35	2	141	2026-01-17 17:37:34.624214	2026-01-19 17:55:54.72784
130	090835e6-af76-4334-843e-5c1eecdcbdf3	2	NYX FAT OIL MISED CALL	28.00	28.00	14.00	14.00	3.65	37	2	81	2025-11-24 18:05:28.954204	2026-01-19 17:55:54.72784
136	9760ffb9-9472-468f-a3f1-41f0b9fce8d8	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	1.83	37	2	87	2025-12-05 18:19:39.262232	2026-01-19 17:55:54.72784
154	4d7a3378-bb98-409c-949a-d56f8b4a21b9	2	NYX FAT OIL MISED CALL	28.00	28.00	14.00	14.00	3.65	37	2	105	2025-12-12 18:21:05.333582	2026-01-19 17:55:54.72784
155	4facf526-bd26-4798-9ca5-08acc995393d	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	1.83	37	2	106	2025-12-12 18:25:06.917216	2026-01-19 17:55:54.72784
158	59f550d9-4769-45ec-bd16-d15cfb231eff	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	1.83	37	2	108	2025-12-12 18:58:16.919837	2026-01-19 17:55:54.72784
173	d05ef1cd-de27-4e72-bb13-7d2e9968230c	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	1.83	37	2	123	2026-01-03 17:19:38.890382	2026-01-19 17:55:54.72784
131	50debb74-c671-4daa-be12-8adb9e5fd0a6	2	NYX FAT OIL FOLLOW BACK	28.00	28.00	14.00	14.00	3.65	38	2	82	2025-11-24 18:08:38.116046	2026-01-19 17:55:54.72784
132	e5270188-b697-46d4-9587-87ff174c87a1	2	NYX FAT OIL THATS CHIC	28.00	28.00	14.00	14.00	3.65	39	2	83	2025-11-24 18:16:21.388489	2026-01-19 17:55:54.72784
133	21200ce7-78fc-44c5-a658-4c9c0d731a00	2	NYX FAT OIL THATS CHIC	28.00	28.00	14.00	14.00	3.65	39	2	84	2025-11-24 18:20:05.711835	2026-01-19 17:55:54.72784
149	97ae6f7d-769b-45c6-b0e2-ac5495bb06b8	1	NYX FAT OIL THATS CHIC	14.00	14.00	14.00	14.00	1.83	39	2	99	2025-12-08 09:28:36.593801	2026-01-19 17:55:54.72784
134	bd525284-478b-4454-9eae-cab3d03d3b81	2	NYX FAT OIL STATUS UPDATE	28.00	28.00	14.00	14.00	3.65	40	2	85	2025-11-24 18:22:43.62353	2026-01-19 17:55:54.72784
150	b00cd757-6291-4402-b989-f4120e4ab856	2	NYX FAT OIL STATUS UPDATE	28.00	28.00	14.00	14.00	3.65	40	2	100	2025-12-08 09:33:20.017271	2026-01-19 17:55:54.72784
167	2aad9d0d-d463-4739-9b72-2cea3ef2baa8	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	117	2025-12-27 14:22:38.880081	2026-01-19 17:55:54.72784
168	f4830f2a-754f-4c29-8c7d-b15785ee4a9b	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	118	2025-12-27 14:28:04.369016	2026-01-19 17:55:54.72784
169	ed4983d7-28cb-4b1f-995a-e93d9bd33253	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	119	2025-12-27 14:31:53.425646	2026-01-19 17:55:54.72784
171	d25541ce-a8d5-4fad-92b2-01ed951d35a9	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	121	2025-12-27 14:46:37.276186	2026-01-19 17:55:54.72784
172	503b42e0-e89d-40a0-9bc9-06507e07b4a6	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	122	2025-12-27 14:49:01.269682	2026-01-19 17:55:54.72784
188	78e80826-ca2d-427d-a67e-4fc2ffd9db5c	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	139	2026-01-15 18:17:23.695514	2026-01-19 17:55:54.72784
189	90feccc2-5503-465a-8234-7a70cb8d5142	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	140	2026-01-17 17:36:13.450223	2026-01-19 17:55:54.72784
191	33f5ed9c-cd32-4e53-af67-39015e1a8a9e	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	142	2026-01-17 17:55:58.16467	2026-01-19 17:55:54.72784
196	b4936265-c02a-470d-a55d-f307cec6ed25	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	147	2026-01-17 18:15:19.153551	2026-01-19 17:55:54.72784
197	8df4f84c-5e07-40fb-a611-a1314e2ffb0c	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	148	2026-01-17 18:23:24.365601	2026-01-19 17:55:54.72784
198	db101145-174d-4a1e-bc0a-7626969e4287	1	MAYBELLINE LIFTER GLOSS AMBER	16.00	16.00	16.00	16.00	2.09	43	2	149	2026-01-17 18:24:45.358577	2026-01-19 17:55:54.72784
184	a0cd7709-b049-4169-bb65-8ddf17ba1a53	1	MAYBELLINE LIFTER GLOSS REEF 	16.00	16.00	16.00	16.00	2.09	44	2	135	2026-01-09 15:44:51.751858	2026-01-19 17:55:54.72784
192	0c6b02f8-47f4-48e9-9d83-0ef7e2062da9	1	MAYBELLINE LIFTER GLOSS REEF 	16.00	16.00	16.00	16.00	2.09	44	2	143	2026-01-17 17:56:37.188019	2026-01-19 17:55:54.72784
182	728eef2a-264c-4793-b797-93169b05acd1	1	MAYBELLINE LIFTER GLOSS TOPAZ	16.00	16.00	16.00	16.00	2.09	45	2	133	2026-01-09 15:32:32.668858	2026-01-19 17:55:54.72784
193	7ed2e20b-858d-4ff8-95a3-53ac9659d564	1	MAYBELLINE LIFTER GLOSS BUBBLEGUM	16.00	16.00	16.00	16.00	2.09	46	2	144	2026-01-17 17:59:51.574133	2026-01-19 17:55:54.72784
135	851a855f-cd37-40ae-b44f-1aba399e88b4	3	MAYBELLINE LIFTER PLUMP HOTCHILI	49.50	49.50	16.50	16.50	6.46	50	2	86	2025-12-01 21:04:23.2871	2026-01-19 17:55:54.72784
162	545f2ffd-859a-473a-8a90-2161d5c09d55	1	MAYBELLINE LIFTER PLUMP HOTCHILI	16.50	16.50	16.50	16.50	2.15	50	2	112	2025-12-13 22:38:53.76615	2026-01-19 17:55:54.72784
137	d1a7d4ca-fc45-44e0-9ceb-7ba1c2ea3582	2	ITALIA DELUXE PHAT LIP GLOW OIL BUZZIN	9.60	9.60	4.80	4.80	1.25	68	2	88	2025-12-05 18:27:57.654137	2026-01-19 17:55:54.72784
138	cd076e78-34e8-45ed-afa2-c50ac12a5651	2	MAXFINE LIP OIL 01	6.60	6.60	3.30	3.30	0.86	69	2	89	2025-12-05 18:32:13.340082	2026-01-19 17:55:54.72784
139	64c68699-931a-4f41-b873-09acdf820b26	1	MAXFINE LIP OIL 03	3.30	3.30	3.30	3.30	0.43	70	2	90	2025-12-05 18:33:50.015134	2026-01-19 17:55:54.72784
186	d613d528-0018-4bcb-b7d8-5ad77e5c9174	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 118	22.00	22.00	22.00	22.00	2.87	90	2	137	2026-01-09 18:12:41.564523	2026-01-19 17:55:54.72784
126	b230fa0c-c7de-449c-afc7-586d9e9506e1	1	MAYBELLINE SUPER STAY LUMI MATTE 30H 220	22.00	22.00	22.00	22.00	2.87	91	2	78	2025-11-14 18:51:14.971909	2026-01-19 17:55:54.72784
157	4b5eabf1-a430-46d0-a6e6-17e5fc42dc63	1	MAYBELLINE FIT ME BASE 130	19.00	19.00	19.00	19.00	2.48	74	2	108	2025-12-12 18:58:16.917825	2026-01-19 17:55:54.72784
183	8f4a468d-1f43-45ea-aa40-736ffec03489	1	MAYBELLINE FIT ME BASE 130	19.00	19.00	19.00	19.00	2.48	74	2	134	2026-01-09 15:44:17.398785	2026-01-19 17:55:54.72784
125	b2ea2185-206a-4532-8a37-bd410bf06122	2	ELF LIP OIL JAM SESSION	26.00	26.00	13.00	13.00	3.39	23	2	77	2025-11-14 18:48:16.608772	2026-01-19 17:55:54.72784
156	9c3076d5-65ee-46f5-8856-806470ebeec3	1	ELF LIP OIL JAM SESSION	13.00	13.00	13.00	13.00	1.70	23	2	107	2025-12-12 18:57:38.803741	2026-01-19 17:55:54.72784
160	dd44f1a1-fb45-4f90-ae06-8ee903daac39	1	ELF LIP OIL JAM SESSION	13.00	13.00	13.00	13.00	1.70	23	2	110	2025-12-13 22:32:21.81456	2026-01-19 17:55:54.72784
161	c1aaaa49-f2b3-4b6a-8382-f9f75dca99a5	1	ELF LIP OIL JAM SESSION	13.00	13.00	13.00	13.00	1.70	23	2	111	2025-12-13 22:32:33.657444	2026-01-19 17:55:54.72784
163	62de6e02-56a9-43ce-9f42-e71bccbdf418	1	ELF LIP OIL JAM SESSION	13.00	13.00	13.00	13.00	1.70	23	2	113	2025-12-13 22:44:13.992057	2026-01-19 17:55:54.72784
152	fdec2a60-5bc2-4975-aa1f-c6a9ee44bfee	2	MAYBELLINE SUPER STAY LUMI MATTE 30H 125	44.00	44.00	22.00	22.00	5.74	94	2	103	2025-12-12 17:55:11.141505	2026-01-19 17:55:54.72784
151	5eba0eea-5c7e-492d-939a-84c8d42ea4eb	2	MAYBELLINE FIT ME BASE 112	38.00	38.00	19.00	19.00	4.96	78	2	102	2025-12-12 17:52:53.585104	2026-01-19 17:55:54.72784
140	6f84bb54-728d-488e-9dcb-82370d3638b1	2	ELF HALO GLOW SKIN TINT 6 LIGHT COOL	42.00	42.00	21.00	21.00	5.48	135	2	91	2025-12-05 18:39:21.420889	2026-01-19 17:55:54.72784
141	337c9d5e-6c29-4dbf-85dc-9ed9bdb523e3	1	ELF HALO GLOW SKIN TINT 7 MEDIUM WARM	21.00	21.00	21.00	21.00	2.74	136	2	92	2025-12-05 18:44:30.128322	2026-01-19 17:55:54.72784
142	d931711a-d7d0-4739-a7c4-7086f681cd1b	1	ELF HALO GLOW SKIN TINT 7 MEDIUM WARM	21.00	21.00	21.00	21.00	2.74	136	2	93	2025-12-05 18:45:47.533642	2026-01-19 17:55:54.72784
143	8b4635c4-1f50-474d-9f94-ca993375fb40	1	ELF HALO GLOW SKIN TINT 7 MEDIUM WARM	21.00	21.00	21.00	21.00	2.74	136	2	94	2025-12-05 18:47:31.075451	2026-01-19 17:55:54.72784
144	c0063d17-0e06-4ceb-9aff-f8bd94f59700	1	ELF HALO GLOW SKIN TINT 7 MEDIUM WARM	21.00	21.00	21.00	21.00	2.74	136	2	95	2025-12-05 18:49:03.871561	2026-01-19 17:55:54.72784
145	89d36fab-7110-4806-b635-d65ddd6b770b	1	MILANI GILDEDMINI EYESHADOW 150 CALL ME OLD FASHIONED 	14.50	14.50	14.50	14.50	1.89	171	2	96	2025-12-05 18:55:46.158753	2026-01-19 17:55:54.72784
146	faa19433-b4c6-4049-9b3f-df10e427ed82	1	MILANI GILDEDMINI EYESHADOW 150 CALL ME OLD FASHIONED 	14.50	14.50	14.50	14.50	1.89	171	2	97	2025-12-05 18:56:43.068784	2026-01-19 17:55:54.72784
174	7f81f85e-1054-4d37-ba61-1d1c5181617f	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	0.00	173	2	125	2026-01-03 18:00:15.793478	2026-01-19 17:55:54.72784
175	23808f5c-2ea0-484e-836c-857fa05a0fd1	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	0.00	173	2	126	2026-01-03 18:02:43.199965	2026-01-19 17:55:54.72784
176	60ceb1e2-8d69-4645-92cc-1a6b044dd547	1	NYX FAT OIL MISED CALL	14.00	14.00	14.00	14.00	0.00	173	2	127	2026-01-03 18:40:37.489284	2026-01-19 17:55:54.72784
147	c95fc3ee-ed79-4b65-9ff3-3a19abba6f96	1	ELF PUTTY BLUSH ISLA DEL SOL	14.00	14.00	14.00	14.00	1.83	225	2	98	2025-12-05 18:59:04.58524	2026-01-19 17:55:54.72784
148	76b1f844-b77f-43e3-b890-418172900ff3	2	ELF LIP OIL SUPER NEUTRAL	26.00	26.00	13.00	13.00	3.39	36	2	99	2025-12-08 09:28:36.585423	2026-01-19 17:55:54.72784
185	ab1a5cb8-6d22-4d3c-bfdb-06f4c1d700b9	1	ELF LIP OIL SUPER NEUTRAL	13.00	13.00	13.00	13.00	1.70	36	2	136	2026-01-09 18:11:15.399078	2026-01-19 17:55:54.72784
239	6bd51cee-9d7f-45ab-98ad-5e9c88b541b6	1	GOT2B GLUE MINI	8.00	8.00	8.00	8.00	1.04	20	2	172	2026-02-13 17:54:49.178809	2026-02-13 17:54:49.178831
251	d3b46a76-0ebd-4ba9-bdcb-56fe67bb09bd	1	ELF LIP OIL CANDY CODED	11.30	11.30	11.30	11.30	1.70	35	2	182	2026-02-26 21:07:30.697032	2026-02-26 21:07:30.697065
\.


--
-- Data for Name: invoice_header; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.invoice_header (invoice_id, invoice_uuid, invoice_status, invoice_tax, invoice_date, invoice_total, invoice_subtotal, invoice_discount, payment_type, sequential, remission_guide, access_key, issue_point, establishment, invoice_type, user_id, client_id, date_created, date_updated, enterprise_id) FROM stdin;
18	b2ea39c9-4f9b-4e33-818c-2a973a7f4c59	AUTHORIZED	\N	2025-11-11	46.00	40.00	0.00	CASH	000000047	001	1111202501100386617300110010010000000473297254617	1	001	FACTURA	1	1	2025-11-11 15:06:12.85305	2025-11-11 15:06:12.853119	2
19	b065776b-cb26-407d-991e-a54c882451dc	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000048	001	1111202501100386617300110010010000000483297254612	1	001	FACTURA	1	1	2025-11-11 15:10:29.23313	2025-11-11 15:10:29.233178	2
20	982b8b77-1f4f-4d62-a95a-795c66beb340	AUTHORIZED	\N	2025-11-11	46.00	40.00	0.00	CASH	000000049	001	1111202501100386617300110010010000000493297254618	1	001	FACTURA	1	1	2025-11-11 15:26:30.780571	2025-11-11 15:26:30.780651	2
1	13eed3f3-fd9c-4dcc-bb11-1314a99a0010	true	15.00	\N	\N	\N	\N	\N	000000022	\N	\N	\N	\N	\N	\N	\N	2025-11-04 15:41:00.239246	2025-11-04 17:20:26.309602	2
21	2be90cbd-0c51-4a8d-acca-d4bd968cc392	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000050	001	1111202501100386617300110010010000000503297254613	1	001	FACTURA	1	1	2025-11-11 16:25:47.280197	2025-11-11 16:25:47.280283	2
22	9fd54ca2-bc51-4c63-90ec-67195412c84f	AUTHORIZED	\N	2025-11-11	39.00	33.91	0.00	CASH	000000051	001	1111202501100386617300110010010000000513297254619	1	001	FACTURA	1	1	2025-11-11 16:29:27.996398	2025-11-11 16:29:27.99646	2
23	98e196ae-c33c-424a-8dec-5d6e05c6e643	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000052	001	1111202501100386617300110010010000000523297254614	1	001	FACTURA	1	1	2025-11-11 16:32:17.516874	2025-11-11 16:32:17.516926	2
24	fcf66382-14ca-416b-b422-40d7fcc8db79	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000053	001	1111202501100386617300110010010000000533297254611	1	001	FACTURA	1	1	2025-11-11 16:36:14.302118	2025-11-11 16:36:14.302196	2
25	eae177f1-e213-40a2-b8f4-3e2f7b00de65	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000054	001	1111202501100386617300110010010000000543297254615	1	001	FACTURA	1	1	2025-11-11 16:45:20.135436	2025-11-11 16:45:20.135482	2
26	227ed989-33a2-4a18-be04-c9f22f9bb770	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000055	001	1111202501100386617300110010010000000553297254610	1	001	FACTURA	1	1	2025-11-11 16:57:21.278444	2025-11-11 16:57:21.278492	2
2	bfd1ea49-d6ed-4dac-9965-29a0c49fdef3	AUTHORIZED	\N	2025-11-04	13.00	11.30	0.00	CASH	000000023	\N	0411202501100386617300110010010000000233297254616	1	\N	FACTURA	\N	\N	2025-11-04 17:21:16.640488	2025-11-05 16:43:52.904473	2
3	44761c19-8e41-4be7-9334-7ccf1ed9e253	AUTHORIZED	\N	2025-11-04	13.00	11.30	0.00	CASH	000000023	\N	0411202501100386617300110010010000000233297254616	1	\N	FACTURA	\N	\N	2025-11-04 17:26:39.061365	2025-11-05 16:43:52.911869	2
4	402934ed-e5ba-4a7a-8b02-22c0875d7f6a	AUTHORIZED	\N	2025-11-04	20.00	17.39	0.00	CASH	000000023	\N	0411202501100386617300110010010000000233297254616	1	\N	FACTURA	\N	\N	2025-11-04 17:29:50.338488	2025-11-05 16:43:52.913503	2
5	5e68fa5f-ed6f-42b7-b42d-cef6210877ea	AUTHORIZED	\N	2025-11-05	13.00	11.30	0.00	CASH	000000024	001	0511202501100386617300110010010000000243297254616	1	001	FACTURA	1	1	2025-11-05 16:48:07.306848	2025-11-05 16:48:07.306923	2
6	2fcbcec5-7f47-4bc8-8800-05e2c5c99859	AUTHORIZED	\N	2025-11-05	53.00	46.08	0.00	CASH	000000028	001	0511202501100386617300110010010000000253297254611	1	001	FACTURA	1	1	2025-11-05 16:50:43.469187	2025-11-05 20:27:50.501711	2
7	a734d737-aa73-4cb0-8f5b-1ae0fae14c63	AUTHORIZED	\N	2025-11-05	46.00	40.00	0.00	CASH	000000029	001	0511202501100386617300110010010000000293297254613	1	001	FACTURA	1	1	2025-11-05 20:28:34.783294	2025-11-05 20:28:34.783339	2
27	8debe103-c66e-40ca-8344-5973379d00a8	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000056	001	1111202501100386617300110010010000000563297254616	1	001	FACTURA	1	1	2025-11-11 17:16:15.604121	2025-11-11 17:16:15.604181	2
28	bdf8c737-3323-43ba-8f0c-72aad6218ca8	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000057	001	1111202501100386617300110010010000000573297254611	1	001	FACTURA	1	1	2025-11-11 17:23:28.703983	2025-11-11 17:23:28.704046	2
29	aadad571-d223-42b5-a492-cf1cad1b9b06	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000058	001	1111202501100386617300110010010000000583297254617	1	001	FACTURA	1	1	2025-11-11 17:35:07.930835	2025-11-11 17:35:07.930883	2
8	c8dbf285-5ec7-4a26-a5e0-60f048d32a1c	AUTHORIZED	\N	2025-11-05	53.00	46.08	0.00	CASH	000000033	001	0511202501100386617300110010010000000303297254619	1	001	FACTURA	1	1	2025-11-05 21:52:39.495973	2025-11-05 22:21:33.870114	2
9	bab5ca8e-7d43-4149-8411-104702f171c8	AUTHORIZED	\N	2025-11-05	26.00	22.61	0.00	CASH	000000035	001	0511202501100386617300110010010000000343297254610	1	001	FACTURA	1	5	2025-11-05 22:22:50.892222	2025-11-05 23:09:52.389457	2
10	ee4489f3-74a0-4d09-9fcc-3c846f8436fb	AUTHORIZED	\N	2025-11-05	26.00	22.61	0.00	CASH	000000036	001	0511202501100386617300110010010000000363297254611	1	001	FACTURA	1	7	2025-11-05 23:10:58.917451	2025-11-05 23:10:58.91749	2
11	2659d321-928c-44e7-a91d-cd4dc3185a11	AUTHORIZED	\N	2025-11-05	26.00	22.61	0.00	CASH	000000037	001	0511202501100386617300110010010000000373297254617	1	001	FACTURA	1	1	2025-11-05 23:21:18.542678	2025-11-05 23:21:18.542735	2
30	181c8ac4-bd1c-4d76-8c20-bbc38509095f	AUTHORIZED	\N	2025-11-11	13.00	11.30	0.00	CASH	000000059	001	1111202501100386617300110010010000000593297254612	1	001	FACTURA	1	1	2025-11-11 17:36:35.144884	2025-11-11 17:36:35.14492	2
12	1f502535-85c7-468e-8fb9-fffee87e7608	AUTHORIZED	\N	2025-11-05	53.00	46.08	0.00	CASH	000000041	001	0511202501100386617300110010010000000383297254612	1	001	FACTURA	1	8	2025-11-05 23:24:16.63564	2025-11-06 13:47:34.689723	2
13	6a94e0f3-ac0a-4422-a63f-2106429b0c3a	AUTHORIZED	\N	2025-11-06	26.00	22.61	0.00	CASH	000000042	001	0611202501100386617300110010010000000423297254619	1	001	FACTURA	1	1	2025-11-06 14:07:23.2758	2025-11-06 14:07:23.275825	2
14	d15c6dd0-7f51-410c-824f-416ce8ffa31f	AUTHORIZED	\N	2025-11-08	26.00	22.61	0.00	CASH	000000043	001	0811202501100386617300110010010000000433297254613	1	001	FACTURA	1	1	2025-11-08 17:09:14.641203	2025-11-08 17:09:14.641276	2
15	02b011df-2235-473a-abca-df655fdd1fb0	AUTHORIZED	\N	2025-11-08	26.00	22.61	0.00	CASH	000000044	001	0811202501100386617300110010010000000443297254619	1	001	FACTURA	1	1	2025-11-08 17:16:30.128142	2025-11-08 17:16:30.128237	2
16	fbf2ab9c-5910-40b3-aace-853e43aa4490	AUTHORIZED	\N	2025-11-08	26.00	22.61	0.00	CASH	000000045	001	0811202501100386617300110010010000000453297254614	1	001	FACTURA	1	1	2025-11-08 17:21:21.220633	2025-11-08 17:21:21.220774	2
17	72244c99-f30c-4bd3-aa84-75140b08ae9b	AUTHORIZED	\N	2025-11-08	13.00	11.30	0.00	CASH	000000046	001	0811202501100386617300110010010000000463297254611	1	001	FACTURA	1	1	2025-11-08 17:24:09.377924	2025-11-08 17:24:09.378003	2
31	99086043-e9d7-41f2-82c7-d6b967bf89b3	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000060	001	1111202501100386617300110010010000000603297254618	1	001	FACTURA	1	1	2025-11-11 17:39:30.736078	2025-11-11 17:39:30.736139	2
32	0223987a-4c24-4875-9670-75f3d6e78820	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000061	001	1111202501100386617300110010010000000613297254613	1	001	FACTURA	1	1	2025-11-11 17:43:25.164054	2025-11-11 17:43:25.164101	2
33	6d0877e9-d963-4875-a7f5-3190c1da2f89	AUTHORIZED	\N	2025-11-11	26.00	22.61	0.00	CASH	000000062	001	1111202501100386617300110010010000000623297254619	1	001	FACTURA	1	1	2025-11-11 18:06:56.142236	2025-11-11 18:06:56.142304	2
34	f1fccece-d87f-4b59-8cd0-98f275d55439	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000063	001	1411202501100386617300110010010000000633297254618	1	001	FACTURA	1	1	2025-11-14 15:40:55.159003	2025-11-14 15:40:55.159075	2
35	cf831ea2-5a84-4964-bc27-ea994b1dbeed	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000064	001	1411202501100386617300110010010000000643297254613	1	001	FACTURA	1	1	2025-11-14 16:46:51.183869	2025-11-14 16:46:51.183924	2
36	56a78ae8-f769-43a1-a49f-8c6a5166bf27	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000065	001	1411202501100386617300110010010000000653297254619	1	001	FACTURA	1	1	2025-11-14 16:50:00.784311	2025-11-14 16:50:00.784384	2
37	1e8740d2-1dfa-4f96-8573-3c05a867d95c	AUTHORIZED	\N	2025-11-14	13.00	11.30	0.00	CASH	000000066	001	1411202501100386617300110010010000000663297254614	1	001	FACTURA	1	1	2025-11-14 16:58:41.288506	2025-11-14 16:58:41.288572	2
114	1d8d2c86-67e6-4c57-bbc1-6bc369704365	VOUCHER	\N	2025-12-27	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:04:55.203157	2025-12-27 14:04:55.203197	2
38	9a5ff583-e68c-421e-bcea-4f13592618c2	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000067	001	1411202501100386617300110010010000000673297254611	1	001	FACTURA	1	1	2025-11-14 17:01:38.345068	2025-11-14 17:01:38.345134	2
39	83a04ff3-874f-4375-bfd0-71f6f6c2a831	AUTHORIZED	\N	2025-11-14	40.00	34.78	0.00	CASH	000000068	001	1411202501100386617300110010010000000683297254615	1	001	FACTURA	1	1	2025-11-14 17:04:02.899429	2025-11-14 17:04:02.899481	2
40	5a89f0e8-9eb4-4107-8150-1ad795441cff	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000069	001	1411202501100386617300110010010000000693297254610	1	001	FACTURA	1	1	2025-11-14 17:08:23.459383	2025-11-14 17:08:23.459455	2
41	448b79c8-0d9c-462d-ab7f-fc96dc757475	AUTHORIZED	\N	2025-11-14	66.00	57.39	0.00	CASH	000000070	001	1411202501100386617300110010010000000703297254616	1	001	FACTURA	1	1	2025-11-14 17:13:05.480488	2025-11-14 17:13:05.480577	2
42	d3fd2f17-b677-4f85-85f3-16aa4423d2e9	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000071	001	1411202501100386617300110010010000000713297254611	1	001	FACTURA	1	1	2025-11-14 17:20:04.818738	2025-11-14 17:20:04.818802	2
43	866823a1-87e3-439a-a7e0-6e6a0209182a	AUTHORIZED	\N	2025-11-14	46.00	40.00	0.00	CASH	000000072	001	1411202501100386617300110010010000000723297254617	1	001	FACTURA	1	1	2025-11-14 17:24:49.804963	2025-11-14 17:24:49.805009	2
44	958eaaf2-1d0e-4c96-bad4-5fd14c65a930	AUTHORIZED	\N	2025-11-14	60.00	52.17	0.00	CASH	000000073	001	1411202501100386617300110010010000000733297254612	1	001	FACTURA	1	1	2025-11-14 17:29:21.66898	2025-11-14 17:29:21.669062	2
45	077920e9-dee2-4905-88b6-5b65fde93ce8	AUTHORIZED	\N	2025-11-14	60.00	52.17	0.00	CASH	000000074	001	1411202501100386617300110010010000000743297254618	1	001	FACTURA	1	1	2025-11-14 17:33:16.187223	2025-11-14 17:33:16.187283	2
46	a2d81e5c-98a9-4e5b-8323-8627ed891b0d	AUTHORIZED	\N	2025-11-14	22.50	19.57	0.00	CASH	000000075	001	1411202501100386617300110010010000000753297254613	1	001	FACTURA	1	1	2025-11-14 17:35:07.614107	2025-11-14 17:35:07.614157	2
47	3491d882-886d-4183-b4e0-7a648fe2777b	AUTHORIZED	\N	2025-11-14	37.50	36.52	0.00	CASH	000000076	001	1411202501100386617300110010010000000763297254619	1	001	FACTURA	1	1	2025-11-14 17:36:53.830595	2025-11-14 17:36:53.830636	2
48	45d42ef1-3051-478e-93d5-5edf06f40563	AUTHORIZED	\N	2025-11-14	22.50	19.57	0.00	CASH	000000077	001	1411202501100386617300110010010000000773297254614	1	001	FACTURA	1	1	2025-11-14 17:39:54.06708	2025-11-14 17:39:54.067136	2
49	dfa2f498-2939-4a6d-a3b9-ac09502e7de1	AUTHORIZED	\N	2025-11-14	22.50	19.57	0.00	CASH	000000078	001	1411202501100386617300110010010000000783297254611	1	001	FACTURA	1	1	2025-11-14 17:41:29.862445	2025-11-14 17:41:29.862511	2
50	2cebe55e-3f09-482c-9bd3-559fdf95f261	AUTHORIZED	\N	2025-11-14	7.50	6.52	0.00	CASH	000000079	001	1411202501100386617300110010010000000793297254615	1	001	FACTURA	1	1	2025-11-14 17:44:29.88043	2025-11-14 17:44:29.880487	2
51	ee1fe4ab-4be4-436a-be8f-1e813e94f922	AUTHORIZED	\N	2025-11-14	7.50	6.52	0.00	CASH	000000080	001	1411202501100386617300110010010000000803297254610	1	001	FACTURA	1	1	2025-11-14 17:50:16.337446	2025-11-14 17:50:16.337529	2
52	a09c052c-4812-409d-85ec-2e7313e21078	AUTHORIZED	\N	2025-11-14	30.00	26.09	0.00	CASH	000000081	001	1411202501100386617300110010010000000813297254616	1	001	FACTURA	1	1	2025-11-14 17:53:32.175519	2025-11-14 17:53:32.175596	2
53	91d0c6a2-b4a8-4ae0-9cd6-68bd1d7230a7	AUTHORIZED	\N	2025-11-14	30.00	26.09	0.00	CASH	000000082	001	1411202501100386617300110010010000000823297254611	1	001	FACTURA	1	1	2025-11-14 17:55:18.841091	2025-11-14 17:55:18.841139	2
54	bb772e66-ab08-4498-8966-5b2d3d73cba3	AUTHORIZED	\N	2025-11-14	30.00	26.09	0.00	CASH	000000083	001	1411202501100386617300110010010000000833297254617	1	001	FACTURA	1	1	2025-11-14 17:55:32.498352	2025-11-14 17:55:32.498391	2
55	5a69b33f-8cbf-4dc8-a141-0734c619e543	AUTHORIZED	\N	2025-11-14	37.50	32.61	0.00	CASH	000000084	001	1411202501100386617300110010010000000843297254612	1	001	FACTURA	1	1	2025-11-14 17:58:15.829723	2025-11-14 17:58:15.829799	2
56	b6ae0417-48af-4b94-97a6-b44eba9371e6	AUTHORIZED	\N	2025-11-14	15.00	13.04	0.00	CASH	000000085	001	1411202501100386617300110010010000000853297254618	1	001	FACTURA	1	1	2025-11-14 17:59:47.572872	2025-11-14 17:59:47.572917	2
57	c0690362-2386-484a-a2e0-f66fe7bf15a2	AUTHORIZED	\N	2025-11-14	15.00	13.04	0.00	CASH	000000086	001	1411202501100386617300110010010000000863297254613	1	001	FACTURA	1	1	2025-11-14 18:03:18.565447	2025-11-14 18:03:18.565559	2
58	15e391d7-d002-42f4-8b94-74dcbe2913be	AUTHORIZED	\N	2025-11-14	7.50	6.52	0.00	CASH	000000087	001	1411202501100386617300110010010000000873297254619	1	001	FACTURA	1	1	2025-11-14 18:06:04.349118	2025-11-14 18:06:04.349161	2
59	69076939-8078-43cd-a4e0-81a8e5554a43	AUTHORIZED	\N	2025-11-14	30.00	26.09	0.00	CASH	000000088	001	1411202501100386617300110010010000000883297254614	1	001	FACTURA	1	1	2025-11-14 18:08:11.503699	2025-11-14 18:08:11.503767	2
60	e6486cb6-9b28-46ca-9c43-6ab26df2c9a7	AUTHORIZED	\N	2025-11-14	7.50	6.52	0.00	CASH	000000089	001	1411202501100386617300110010010000000893297254611	1	001	FACTURA	1	1	2025-11-14 18:11:19.437179	2025-11-14 18:11:19.437226	2
61	740f757a-8573-43a6-b406-490be58b7e72	AUTHORIZED	\N	2025-11-14	40.50	35.21	0.00	CASH	000000090	001	1411202501100386617300110010010000000903297254615	1	001	FACTURA	1	1	2025-11-14 18:14:53.791314	2025-11-14 18:14:53.791401	2
62	b933668b-ce3a-44a7-8031-7b4b65213fac	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000091	001	1411202501100386617300110010010000000913297254610	1	001	FACTURA	1	1	2025-11-14 18:17:11.984045	2025-11-14 18:17:11.984117	2
63	4ad3542c-c22d-446a-be24-33ff67839612	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000092	001	1411202501100386617300110010010000000923297254616	1	001	FACTURA	1	1	2025-11-14 18:18:01.777591	2025-11-14 18:18:01.77762	2
64	0c8ad21a-cdac-47de-b4a8-9158f51db9bb	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000093	001	1411202501100386617300110010010000000933297254611	1	001	FACTURA	1	1	2025-11-14 18:19:24.213698	2025-11-14 18:19:24.213742	2
65	4f05eef8-0b68-4919-a7e0-17eeeca2677f	AUTHORIZED	\N	2025-11-14	37.50	32.61	0.00	CASH	000000094	001	1411202501100386617300110010010000000943297254617	1	001	FACTURA	1	1	2025-11-14 18:20:54.519723	2025-11-14 18:20:54.519788	2
66	26ccecdf-18fc-4395-93f8-ba097d7f91ca	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000095	001	1411202501100386617300110010010000000953297254612	1	001	FACTURA	1	1	2025-11-14 18:22:51.576039	2025-11-14 18:22:51.576119	2
67	179e91e5-ea39-4a89-b1f6-386b7cba163c	AUTHORIZED	\N	2025-11-14	22.50	21.52	0.00	CASH	000000096	001	1411202501100386617300110010010000000963297254618	1	001	FACTURA	1	1	2025-11-14 18:23:16.124396	2025-11-14 18:23:16.124429	2
68	479410c1-7b80-48e6-9481-7e378e8967bd	AUTHORIZED	\N	2025-11-14	20.50	17.82	0.00	CASH	000000097	001	1411202501100386617300110010010000000973297254613	1	001	FACTURA	1	1	2025-11-14 18:24:34.200355	2025-11-14 18:24:34.200423	2
69	67cdc77f-6fe0-4501-8cd7-af02910c0f79	AUTHORIZED	\N	2025-11-14	7.50	6.52	0.00	CASH	000000098	001	1411202501100386617300110010010000000983297254619	1	001	FACTURA	1	1	2025-11-14 18:25:48.715914	2025-11-14 18:25:48.715981	2
70	0b304ab9-5041-4500-a162-fa417f352962	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000099	001	1411202501100386617300110010010000000993297254614	1	001	FACTURA	1	1	2025-11-14 18:26:31.237274	2025-11-14 18:26:31.237315	2
71	0c239cfb-dc3b-4625-aac0-1134a9f6935c	AUTHORIZED	\N	2025-11-14	7.50	6.52	0.00	CASH	000000100	001	1411202501100386617300110010010000001003297254618	1	001	FACTURA	1	1	2025-11-14 18:27:03.938745	2025-11-14 18:27:03.938765	2
73	61519d6e-03fb-4ad6-b76c-11cfa24d3b6b	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000102	001	1411202501100386617300110010010000001023297254619	1	001	FACTURA	1	1	2025-11-14 18:29:24.920967	2025-11-14 18:29:24.921008	2
74	64a0385d-4abc-4504-ad2d-b8cdcc828a1c	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000103	001	1411202501100386617300110010010000001033297254614	1	001	FACTURA	1	1	2025-11-14 18:30:21.398192	2025-11-14 18:30:21.398243	2
75	f1d03dfe-051c-45a5-b406-035ba3d8995c	AUTHORIZED	\N	2025-11-14	88.50	78.91	0.00	CASH	000000104	001	1411202501100386617300110010010000001043297254611	1	001	FACTURA	1	1	2025-11-14 18:35:14.841408	2025-11-14 18:35:14.841474	2
76	122594d7-e738-4b88-885b-b73dec384f7d	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000105	001	1411202501100386617300110010010000001053297254615	1	001	FACTURA	1	1	2025-11-14 18:38:04.40238	2025-11-14 18:38:04.40244	2
77	9ecacc62-4147-40fd-9611-706f45dd49ea	AUTHORIZED	\N	2025-11-14	26.00	22.61	0.00	CASH	000000106	001	1411202501100386617300110010010000001063297254610	1	001	FACTURA	1	4	2025-11-14 18:48:16.545555	2025-11-14 18:48:16.545574	2
78	8e544485-fd1c-4e56-b3ff-982cf4aaa419	AUTHORIZED	\N	2025-11-14	28.00	26.09	0.00	CASH	000000107	001	1411202501100386617300110010010000001073297254616	1	001	FACTURA	1	4	2025-11-14 18:51:14.958874	2025-11-14 18:51:14.958878	2
79	e919c2b6-8eed-495b-9aba-e0e11e08a043	AUTHORIZED	\N	2025-11-24	26.00	22.61	0.00	CASH	000000108	001	2411202501100386617300110010010000001083297254615	1	001	FACTURA	1	1	2025-11-24 17:54:40.122705	2025-11-24 17:54:40.122763	2
80	5187b92a-3f61-4d89-9882-b2cc0daa2152	AUTHORIZED	\N	2025-11-24	26.00	22.61	0.00	CASH	000000109	001	2411202501100386617300110010010000001093297254610	1	001	FACTURA	1	1	2025-11-24 17:57:43.174934	2025-11-24 17:57:43.174961	2
81	7a7cf6cc-a132-4aea-bc03-1a47ad1f682f	AUTHORIZED	\N	2025-11-24	28.00	24.35	0.00	CASH	000000110	001	2411202501100386617300110010010000001103297254616	1	001	FACTURA	1	1	2025-11-24 18:05:28.849548	2025-11-24 18:05:28.849626	2
82	d09e3264-6709-4c95-984a-db190c5c0b86	AUTHORIZED	\N	2025-11-24	28.00	24.35	0.00	CASH	000000111	001	2411202501100386617300110010010000001113297254611	1	001	FACTURA	1	1	2025-11-24 18:08:38.019673	2025-11-24 18:08:38.019755	2
83	5c3d9421-f524-4a06-afff-3a90c4fb43c0	AUTHORIZED	\N	2025-11-24	28.00	24.35	0.00	CASH	000000112	001	2411202501100386617300110010010000001123297254617	1	001	FACTURA	1	1	2025-11-24 18:16:21.347857	2025-11-24 18:16:21.347952	2
84	da3e72a0-1119-439b-ad21-69d34070b480	AUTHORIZED	\N	2025-11-24	28.00	24.35	0.00	CASH	000000113	001	2411202501100386617300110010010000001133297254612	1	001	FACTURA	1	1	2025-11-24 18:20:05.687518	2025-11-24 18:20:05.687582	2
85	dd91d9af-1180-4895-b3b2-93fe3fd88ad9	AUTHORIZED	\N	2025-11-24	28.00	24.35	0.00	CASH	000000114	001	2411202501100386617300110010010000001143297254618	1	001	FACTURA	1	1	2025-11-24 18:22:43.592667	2025-11-24 18:22:43.59273	2
86	e61f6f21-6a63-40f1-995d-a8b23cdb813c	AUTHORIZED	\N	2025-12-01	49.50	43.04	0.00	CASH	000000115	001	0112202501100386617300110010010000001153297254619	1	001	FACTURA	1	5	2025-12-01 21:04:23.160581	2025-12-01 21:04:23.160686	2
87	85a8bd96-3ce9-4501-a71c-312a5313eb09	AUTHORIZED	\N	2025-12-05	14.00	12.17	0.00	CASH	000000116	001	0512202501100386617300110010010000001163297254612	1	001	FACTURA	1	2	2025-12-05 18:19:39.183043	2025-12-05 18:19:39.183106	2
88	4de16b08-eefd-4334-9f3a-466fc3bcf903	AUTHORIZED	\N	2025-12-05	9.60	8.35	0.00	CASH	000000117	001	0512202501100386617300110010010000001173297254618	1	001	FACTURA	1	2	2025-12-05 18:27:57.63986	2025-12-05 18:27:57.639882	2
89	fb298d64-e96d-4b27-8c62-a2881cde42d0	AUTHORIZED	\N	2025-12-05	6.60	5.74	0.00	CASH	000000118	001	0512202501100386617300110010010000001183297254613	1	001	FACTURA	1	2	2025-12-05 18:32:13.322098	2025-12-05 18:32:13.322123	2
90	a2f3b463-042d-4252-b49c-1591c8287ea2	AUTHORIZED	\N	2025-12-05	3.30	2.87	0.00	CASH	000000119	001	0512202501100386617300110010010000001193297254619	1	001	FACTURA	1	2	2025-12-05 18:33:49.944616	2025-12-05 18:33:49.944649	2
91	ce30bded-1a29-47e3-8fb8-580a867b9a0f	AUTHORIZED	\N	2025-12-05	42.00	36.52	0.00	CASH	000000120	001	0512202501100386617300110010010000001203297254614	1	001	FACTURA	1	2	2025-12-05 18:39:21.408621	2025-12-05 18:39:21.408648	2
92	a8b56ad6-d5d4-49c5-ad1f-d041a5e172f9	AUTHORIZED	\N	2025-12-05	21.00	18.26	0.00	CASH	000000121	001	0512202501100386617300110010010000001213297254611	1	001	FACTURA	1	2	2025-12-05 18:44:30.113214	2025-12-05 18:44:30.113271	2
93	34d6faa8-cabc-425d-bdea-fe485560ff8d	AUTHORIZED	\N	2025-12-05	21.00	18.26	0.00	CASH	000000122	001	0512202501100386617300110010010000001223297254615	1	001	FACTURA	1	2	2025-12-05 18:45:47.522676	2025-12-05 18:45:47.522699	2
94	70311986-b3fa-4ea1-a318-6dfdc2a14d8d	AUTHORIZED	\N	2025-12-05	21.00	18.26	0.00	CASH	000000123	001	0512202501100386617300110010010000001233297254610	1	001	FACTURA	1	2	2025-12-05 18:47:31.058773	2025-12-05 18:47:31.058817	2
95	1754825e-d15f-4449-88b5-3910847f3899	AUTHORIZED	\N	2025-12-05	21.00	18.26	0.00	CASH	000000124	001	0512202501100386617300110010010000001243297254616	1	001	FACTURA	1	2	2025-12-05 18:49:03.808765	2025-12-05 18:49:03.808797	2
96	316dd8eb-5d73-4dab-8e23-36d56c5d0480	AUTHORIZED	\N	2025-12-05	14.50	12.61	0.00	CASH	000000125	001	0512202501100386617300110010010000001253297254611	1	001	FACTURA	1	2	2025-12-05 18:55:46.14129	2025-12-05 18:55:46.141333	2
97	97bae131-22e9-4cea-af02-7ef6f67796c5	AUTHORIZED	\N	2025-12-05	14.50	12.61	0.00	CASH	000000126	001	0512202501100386617300110010010000001263297254617	1	001	FACTURA	1	2	2025-12-05 18:56:42.972256	2025-12-05 18:56:42.972302	2
98	cf2d46a8-ee72-438b-a0db-ccdb6147e029	AUTHORIZED	\N	2025-12-05	14.00	12.17	0.00	CASH	000000127	001	0512202501100386617300110010010000001273297254612	1	001	FACTURA	1	1	2025-12-05 18:59:04.57089	2025-12-05 18:59:04.570917	2
99	66e85b6d-b2da-4b95-884b-3eb14689de64	AUTHORIZED	\N	2025-12-08	40.00	34.78	0.00	CASH	000000128	001	0812202501100386617300110010010000001283297254611	1	001	FACTURA	1	1	2025-12-08 09:28:36.426697	2025-12-08 09:28:36.426801	2
100	bc4a8d51-527c-42d8-93d0-7183799a8f51	AUTHORIZED	\N	2025-12-08	28.00	24.35	0.00	CASH	000000129	001	0812202501100386617300110010010000001293297254617	1	001	FACTURA	1	2	2025-12-08 09:33:19.995292	2025-12-08 09:33:19.995316	2
102	823e1c6b-f854-4308-a1ae-4b876f7e3aeb	AUTHORIZED	\N	2025-12-12	38.00	33.04	0.00	CASH	000000130	001	1212202501100386617300110010010000001303297254619	1	001	FACTURA	1	2	2025-12-12 17:52:53.564696	2025-12-12 17:52:53.564728	2
103	0d3a4ecb-8694-43f3-8faa-688172f33e9a	AUTHORIZED	\N	2025-12-12	44.00	38.26	0.00	CASH	000000131	001	1212202501100386617300110010010000001313297254614	1	001	FACTURA	1	2	2025-12-12 17:55:11.127913	2025-12-12 17:55:11.127939	2
104	aa0b841b-c518-427f-8b59-06b11672c477	AUTHORIZED	\N	2025-12-12	13.00	11.30	0.00	CASH	000000132	001	1212202501100386617300110010010000001323297254611	1	001	FACTURA	1	6	2025-12-12 18:20:03.942163	2025-12-12 18:20:03.942263	2
105	2a19738b-da42-48a7-8527-428737cfcbae	AUTHORIZED	\N	2025-12-12	28.00	24.35	0.00	CASH	000000133	001	1212202501100386617300110010010000001333297254615	1	001	FACTURA	1	1	2025-12-12 18:21:05.316618	2025-12-12 18:21:05.316655	2
106	87c30aee-6261-4f1d-9bc4-91e52500e9ab	AUTHORIZED	\N	2025-12-12	14.00	12.17	0.00	CASH	000000134	001	1212202501100386617300110010010000001343297254610	1	001	FACTURA	1	2	2025-12-12 18:25:06.901448	2025-12-12 18:25:06.901483	2
107	f0fcca3d-920f-4176-bc46-7d42a7783c65	AUTHORIZED	\N	2025-12-12	13.00	11.30	0.00	CASH	000000135	001	1212202501100386617300110010010000001353297254616	1	001	FACTURA	1	2	2025-12-12 18:57:38.742906	2025-12-12 18:57:38.742979	2
108	cc1bf198-76a8-440f-950f-671d5f2d1ed5	AUTHORIZED	\N	2025-12-12	33.00	28.69	0.00	CASH	000000136	001	1212202501100386617300110010010000001363297254611	1	001	FACTURA	1	2	2025-12-12 18:58:16.901479	2025-12-12 18:58:16.901499	2
109	a5d879eb-c9ca-40e5-aa5b-07c41389d960	AUTHORIZED	\N	2025-12-12	19.00	16.52	0.00	CASH	000000137	001	1212202501100386617300110010010000001373297254617	1	001	FACTURA	1	2	2025-12-12 19:16:26.555591	2025-12-12 19:16:26.555611	2
110	abd49d43-4109-4f27-bb9a-4de1186c3fc7	VOUCHER	\N	2025-12-13	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2025-12-13 22:32:21.762484	2025-12-13 22:32:21.762537	2
111	de4e7517-6b4c-4fd4-a365-cee0034bcf72	VOUCHER	\N	2025-12-13	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2025-12-13 22:32:33.640527	2025-12-13 22:32:33.640552	2
112	253b58cd-ec52-415e-8959-7a565af02bed	VOUCHER	\N	2025-12-13	16.50	14.35	0.00	CASH				1		VOUCHER	1	\N	2025-12-13 22:38:53.70749	2025-12-13 22:38:53.707542	2
113	280a1116-2449-467d-8964-b68bc04ca54d	VOUCHER	\N	2025-12-13	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2025-12-13 22:44:13.925383	2025-12-13 22:44:13.925473	2
115	bb50bd31-1edb-4da2-84f1-348a38978f14	VOUCHER	\N	2025-12-27	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:06:24.971611	2025-12-27 14:06:24.971632	2
116	998986ca-011a-4711-ba2c-b3284aa8a2ef	VOUCHER	\N	2025-12-27	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:20:56.380089	2025-12-27 14:20:56.380127	2
117	87c73c59-a3bc-40c3-9ce9-9fc5778596da	VOUCHER	\N	2025-12-27	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:22:38.83287	2025-12-27 14:22:38.832914	2
118	488d9c37-16c6-4730-80af-91d843f075ae	VOUCHER	\N	2025-12-27	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:28:04.323325	2025-12-27 14:28:04.323364	2
119	2f247c83-3d62-4923-9d5c-11f0becd0f25	VOUCHER	\N	2025-12-27	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:31:53.381661	2025-12-27 14:31:53.381699	2
120	3b3f965f-bcff-45f8-8bb5-351f148ef938	VOUCHER	\N	2025-12-27	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:39:47.888162	2025-12-27 14:39:47.888199	2
121	e1f59b52-f01c-4958-919c-65cb2c664e93	VOUCHER	\N	2025-12-27	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:46:37.210961	2025-12-27 14:46:37.211014	2
122	1a174c11-ed34-4db0-ab09-0f5d9647c1c6	VOUCHER	\N	2025-12-27	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2025-12-27 14:49:01.230025	2025-12-27 14:49:01.230081	2
124	789bb339-a652-476d-a7ae-17f45f15f2b1	VOUCHER	\N	2026-01-03	50.00	47.13	0.00	CASH				1		VOUCHER	1	\N	2026-01-03 17:40:36.216757	2026-01-03 17:40:36.216779	2
125	006cbde6-aca3-4cfd-8bb4-8c43c164c809	VOUCHER	\N	2026-01-03	14.00	14.00	0.00	CASH				1		VOUCHER	1	\N	2026-01-03 18:00:15.779705	2026-01-03 18:00:15.779731	2
126	ff37a3ca-0f3d-4362-a2c6-68379691d27b	VOUCHER	\N	2026-01-03	14.00	14.00	0.00	CASH				1		VOUCHER	1	\N	2026-01-03 18:02:43.124249	2026-01-03 18:02:43.124281	2
127	ca5f475a-43f4-4bdb-b42b-8c4ebba510a1	VOUCHER	\N	2026-01-03	14.00	14.00	0.00	CASH				1		VOUCHER	1	\N	2026-01-03 18:40:37.47208	2026-01-03 18:40:37.472115	2
130	297be789-8ed3-4d6d-808f-0a1513f7c947	VOUCHER	\N	2026-01-06	11.50	11.30	1.50	CASH				1		VOUCHER	1	\N	2026-01-06 00:03:09.268051	2026-01-06 00:03:09.26809	2
131	bff0020c-159c-4ab2-9ed9-1537716062cc	VOUCHER	\N	2026-01-06	11.50	11.30	1.50	CASH				1		VOUCHER	1	\N	2026-01-06 00:06:37.13792	2026-01-06 00:06:37.137964	2
132	2cfe002c-13e2-492d-9955-aeac11c0eedd	VOUCHER	\N	2026-01-09	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 12:56:54.180989	2026-01-09 12:56:54.181011	2
133	c643298b-e295-4793-b3cb-5f50f8b69bee	VOUCHER	\N	2026-01-09	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 15:32:32.657796	2026-01-09 15:32:32.657819	2
134	e7b9bf56-905a-419e-9a85-c9f03746d294	VOUCHER	\N	2026-01-09	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 15:44:17.389784	2026-01-09 15:44:17.389814	2
135	552b2091-c6f0-462d-9ad9-99906f1c5ada	VOUCHER	\N	2026-01-09	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 15:44:51.746133	2026-01-09 15:44:51.746159	2
136	753fdf1b-33f3-4070-acde-9378e06c632a	VOUCHER	\N	2026-01-09	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 18:11:15.346557	2026-01-09 18:11:15.346666	2
137	2ee21170-9948-4f68-acf8-d965e11d03dd	VOUCHER	\N	2026-01-09	22.00	19.13	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 18:12:41.540056	2026-01-09 18:12:41.540131	2
138	d965425d-da21-4592-bd3b-e5b80c107721	VOUCHER	\N	2026-01-09	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2026-01-09 18:13:20.818779	2026-01-09 18:13:20.818858	2
139	2efada25-03e0-4ca7-89ec-82c82b86370e	VOUCHER	\N	2026-01-15	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-15 18:17:23.549879	2026-01-15 18:17:23.549914	2
140	994fe10f-9942-44cc-8e59-e34981b0993e	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 17:36:13.406802	2026-01-17 17:36:13.406862	2
141	c163673f-9164-49bf-9ff3-16055bd21a45	VOUCHER	\N	2026-01-17	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 17:37:34.610454	2026-01-17 17:37:34.610476	2
142	52dde8ee-e377-42aa-99fe-9c152f173a9e	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 17:55:58.113205	2026-01-17 17:55:58.113225	2
143	ea660b43-cb02-4259-b0a8-eed3e8144d66	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 17:56:37.175019	2026-01-17 17:56:37.175045	2
144	5ec325fa-05f3-42a7-ae13-9f380a34bd1b	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 17:59:51.562429	2026-01-17 17:59:51.562451	2
145	ab86a136-13a8-44b5-92ac-31ca4bb3bd56	VOUCHER	\N	2026-01-17	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 18:09:43.42705	2026-01-17 18:09:43.427075	2
146	7cbf4ac7-8466-4728-9d42-242c4ad4277b	VOUCHER	\N	2026-01-17	19.00	16.52	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 18:10:08.152382	2026-01-17 18:10:08.152403	2
147	d32de0b6-13c1-4f04-8ac4-8a33d18ed23f	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 18:15:19.144579	2026-01-17 18:15:19.144604	2
148	df1e71d6-3878-4466-856a-b24f70ce2ae3	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 18:23:24.353661	2026-01-17 18:23:24.353681	2
149	58eb01a4-0b89-43aa-8316-0972cbfef6f8	VOUCHER	\N	2026-01-17	16.00	13.91	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 18:24:45.346564	2026-01-17 18:24:45.346583	2
150	e35d9f07-1d82-4eba-97a0-87dbc729d70d	VOUCHER	\N	2026-01-17	13.00	11.30	0.00	CASH				1		VOUCHER	1	\N	2026-01-17 18:26:27.684939	2026-01-17 18:26:27.684962	2
151	60f2aacc-61c9-4fdf-b814-d27f9b2b0969	VOUCHER	\N	2026-02-03	242.09	215.59	5.86	CASH				1		VOUCHER	1	\N	2026-02-03 18:01:25.107305	2026-02-03 18:01:25.107348	2
152	2ed41cc7-77cd-4245-a345-90556d0f1816	VOUCHER	\N	2026-02-03	0.00	0.00	0.00	CASH				1		VOUCHER	1	\N	2026-02-03 21:51:56.39461	2026-02-03 21:51:56.39465	2
153	c0b76fd0-2e31-4e60-8979-f8f68f9195f0	VOUCHER	\N	2026-02-03	17.00	17.00	0.00	CASH				1		VOUCHER	1	\N	2026-02-03 21:52:48.647368	2026-02-03 21:52:48.647388	2
154	c888cb8a-5771-4818-a902-b994f814d5f8	VOUCHER	\N	2026-02-03	267.00	250.62	0.00	CASH				1		VOUCHER	1	\N	2026-02-03 22:13:54.242063	2026-02-03 22:13:54.242088	2
155	3ea1ce1e-cb0c-4bfc-89e5-bd904f86fa5c	VOUCHER	\N	2026-02-03	40.00	34.78	0.00	CASH				1		VOUCHER	1	\N	2026-02-03 23:11:39.334322	2026-02-03 23:11:39.334386	2
156	204f366b-8346-4544-ac0a-87000f1ee089	VOUCHER	\N	2026-02-03	32.00	27.83	0.00	CASH				1		VOUCHER	1	\N	2026-02-03 23:12:16.99506	2026-02-03 23:12:16.995081	2
128	14422e29-e874-4510-ae51-7f0810b8c56e	SAVED	\N	2026-01-03	19.00	16.52	0.00	CASH				1	VANE	SAVED	1	\N	2026-01-03 18:41:33.514659	2026-02-07 16:05:48.735835	2
123	9d120b93-5597-4bfb-817f-3b7f52dc75c4	SAVED	\N	2026-01-03	14.00	12.17	0.00	CASH				1	ANDY	SAVED	1	\N	2026-01-03 17:19:38.821825	2026-02-07 16:06:24.835543	2
157	df015bb8-d5a6-400f-86bc-dcdaeab810c8	SAVED	\N	2026-02-07	15.45	13.91	0.55	CASH				1	Veci	SAVED	1	\N	2026-02-07 17:06:24.943872	2026-02-07 17:06:24.943939	2
129	e17de923-a3c1-4303-b8e3-a0bbba9c26f7	VOUCHER	\N	2026-02-07	14.00	14.00	0.00	CASH				1		VOUCHER	1	\N	2026-01-03 18:42:22.16023	2026-02-07 17:50:34.243071	2
72	49884b21-98ea-480d-9cfa-797e5189d85a	AUTHORIZED	\N	2025-11-14	55.50	50.21	0.00	CASH	000000101	001	1411202501100386617300110010010000001013297254613	1	001	FACTURA	1	4	2025-11-14 18:28:14.902552	2026-02-11 13:55:28.346097	2
158	f44fecac-401d-4247-b34c-fa97da9650ad	true	15.00	\N	\N	\N	\N	\N	000000000	\N	\N	2	\N	FACTURA	\N	\N	2026-02-12 17:43:40.495381	2026-02-12 17:46:16.603425	1
159	42b7686e-14e9-49fc-aff3-f117e7f68886	AUTHORIZED	\N	2026-02-12	13.00	11.30	0.00	CASH	000000001	001	1202202601171624794300110010010000000013297254617	2	001	FACTURA	2	4	2026-02-12 17:47:06.328509	2026-02-12 17:47:06.328608	1
160	25c3d2f3-1f8d-42f6-994b-ed80472929b8	AUTHORIZED	\N	2026-02-12	13.00	11.30	0.00	CASH	000000003	001	1202202601171624794300110010010000000023297254612	2	001	FACTURA	2	4	2026-02-12 18:00:15.658767	2026-02-12 18:22:09.510767	1
161	f30a306b-8073-46e2-9136-80cd98a05b0c	AUTHORIZED	\N	2026-02-12	14.00	12.17	0.00	CASH	000000004	001	1202202601171624794300110010010000000043297254613	2	001	FACTURA	2	4	2026-02-12 18:22:41.305832	2026-02-12 18:22:41.305951	1
162	cb3da8ba-f131-447b-8903-ff3ddf33fb2d	AUTHORIZED	\N	2026-02-12	17.85	15.52	0.00	CASH	000000005	001	1202202601171624794300110010010000000053297254619	2	001	FACTURA	2	4	2026-02-12 18:59:38.251382	2026-02-12 18:59:38.251459	1
163	333edcc9-768f-42ae-bf2a-fdac39ea4586	AUTHORIZED	\N	2026-02-13	115.15	100.12	0.00	CASH	000000006	001	1302202601171624794300110010010000000063297254619	2	001	FACTURA	2	4	2026-02-13 13:41:48.526135	2026-02-13 13:41:48.526175	1
164	76760c22-d344-4056-a34b-9e5cc0cd5818	AUTHORIZED	\N	2026-02-13	19.00	16.52	0.00	CASH	000000007	001	1302202601171624794300110010010000000073297254614	2	001	FACTURA	2	4	2026-02-13 13:47:57.011452	2026-02-13 13:47:57.011473	1
165	8d65df00-5127-4b3f-8673-734e9737e99c	true	\N	\N	\N	\N	\N	\N	000000087	\N	\N	2	\N	FACTURA	\N	\N	2026-02-13 13:58:31.640251	2026-02-13 13:59:33.571855	1
166	71e263f1-fe6f-4c73-abb4-b687cd38be9f	AUTHORIZED	\N	2026-02-13	115.15	100.12	0.00	CASH	000000088	001	1302202601171624794300120010010000000883297254614	2	001	FACTURA	2	11	2026-02-13 14:23:08.128412	2026-02-13 14:23:08.128438	1
167	c3cea173-3587-4a63-80c2-cb8c21f22b1f	true	15.00	\N	\N	\N	\N	\N	000000008	\N	\N	2	\N	FACTURA	\N	\N	2026-02-13 15:53:13.969305	2026-02-13 16:07:55.168107	1
168	bfb13a3a-2bab-4b57-bf84-436a1d6cc223	AUTHORIZED	\N	2026-02-13	16.00	13.91	0.00	CASH	000000009	001	1302202601171624794300110010010000000093297254615	2	001	FACTURA	2	4	2026-02-13 16:08:26.944788	2026-02-13 16:08:26.944835	1
169	aee33002-2db4-428f-8382-a858b3b84cf0	true	\N	\N	\N	\N	\N	\N	000000013	\N	\N	2	\N	FACTURA	\N	\N	2026-02-13 16:21:39.667438	2026-02-13 16:33:20.483778	1
170	7f0460c4-9a67-4747-855f-4e35b1eddc94	AUTHORIZED	\N	2026-02-13	16.00	13.91	0.00	CASH	000000014	001	1302202601171624794300110010010000000143297254612	2	001	FACTURA	2	4	2026-02-13 16:58:01.277784	2026-02-13 16:58:01.277822	1
171	d6c00230-f10b-4053-b150-add7e50644d4	AUTHORIZED	\N	2026-02-13	13.00	11.30	0.00	CASH	000000015	001	1302202601171624794300110010010000000153297254618	2	001	FACTURA	2	4	2026-02-13 17:10:02.877257	2026-02-13 17:10:02.877312	1
172	3f8eb1fa-dcf1-4eb0-b75c-3528edd8f78a	VOUCHER	\N	2026-02-13	8.00	6.96	0.00	CASH				2		VOUCHER	2	\N	2026-02-13 17:54:49.160751	2026-02-13 17:54:49.160778	1
173	46484c6b-b200-4692-bca5-ddf6366159d9	VOUCHER	\N	2026-02-14	60.90	55.55	0.00	CASH				2		VOUCHER	2	\N	2026-02-14 13:26:59.560831	2026-02-14 13:26:59.560869	1
174	914563b6-ee28-46c0-bbab-32d7d8172f89	true	15.00	\N	\N	\N	\N	\N	000000016	\N	\N	2	\N	FACTURA	\N	\N	2026-02-14 16:20:00.164581	2026-02-14 16:20:00.164581	1
175	f66d47e4-025e-4a3e-a566-fc2236fe480a	AUTHORIZED	\N	2026-02-25	3.30	2.87	0.00	CASH	000000017	001	2502202601171624794300110010010000000173297254611	2	001	FACTURA	2	4	2026-02-25 13:47:07.920607	2026-02-25 13:47:07.920723	1
176	e3478b10-4a54-41a9-a504-9a808456abb8	AUTHORIZED	\N	2026-02-25	13.00	11.30	0.00	CASH	000000018	001	2502202601171624794300110010010000000183297254617	2	001	FACTURA	2	4	2026-02-25 13:53:02.870296	2026-02-25 13:53:02.870374	1
177	561161b4-3c2c-4838-b8d6-3ee2e4a5c11b	AUTHORIZED	\N	2026-02-26	13.00	11.30	0.00	CASH	000000019	001	2602202601171624794300110010010000000193297254617	2	001	FACTURA	2	4	2026-02-26 08:30:30.416269	2026-02-26 08:30:30.416319	1
178	01379d1c-1e1d-484c-b52c-65256318bbf8	AUTHORIZED	\N	2026-02-26	16.00	13.91	0.00	CASH	000000020	001	2602202601171624794300110010010000000203297254612	2	001	FACTURA	2	4	2026-02-26 17:10:24.678189	2026-02-26 17:10:24.678237	1
179	a8c20a52-c9b0-46ec-b0bb-2adc1e192272	AUTHORIZED	\N	2026-02-26	13.00	11.30	0.00	CASH	000000021	001	2602202601171624794300110010010000000213297254618	2	001	FACTURA	2	4	2026-02-26 17:45:14.517391	2026-02-26 17:45:14.51742	1
180	9436289f-328f-4f0a-acb7-8c7c6b26961e	AUTHORIZED	\N	2026-02-26	20.00	17.39	0.00	CASH	000000022	001	2602202601171624794300110010010000000223297254613	2	001	FACTURA	2	4	2026-02-26 17:52:48.970391	2026-02-26 17:52:48.970447	1
181	9ab1f1a0-7c9a-43f4-9081-f246829882f3	AUTHORIZED	\N	2026-02-26	16.00	13.91	0.00	CASH	000000023	001	2602202601171624794300110010010000000233297254619	2	001	FACTURA	2	4	2026-02-26 18:01:45.877641	2026-02-26 18:01:45.877712	1
182	72a3b06c-f3ba-468a-a21f-7bf5c3549cdd	AUTHORIZED	\N	2026-02-26	13.00	11.30	0.00	CASH	000000024	001	2602202601171624794300110010010000000243297254614	2	001	FACTURA	2	4	2026-02-26 21:07:30.55859	2026-02-26 21:07:30.558707	1
\.


--
-- Data for Name: invoice_temp_authorization; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.invoice_temp_authorization (temp_id, temp_uuid, file_base64, access_code, reception_status, authorization_status, enterprise_id, outlet_id, invoice_id, date_created, date_updated) FROM stdin;
4	981ae850-c541-45df-b8f5-0794b6f3f99a	PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48ZmFjdHVyYSBpZD0iY29tcHJvYmFudGUiIHZlcnNpb249IjIuMC4wIj4KICAgIDxpbmZvVHJpYnV0YXJpYT4KICAgICAgICA8YW1iaWVudGU+MTwvYW1iaWVudGU+CiAgICAgICAgPHRpcG9FbWlzaW9uPjE8L3RpcG9FbWlzaW9uPgogICAgICAgIDxyYXpvblNvY2lhbD5DQUJBU0NBTkdPIEFOUkFOR08gR0lTU0VMIFZBTkVTU0E8L3Jhem9uU29jaWFsPgogICAgICAgIDxub21icmVDb21lcmNpYWw+R0lMw5o8L25vbWJyZUNvbWVyY2lhbD4KICAgICAgICA8cnVjPjE3MTYyNDc5NDMwMDE8L3J1Yz4KICAgICAgICA8Y2xhdmVBY2Nlc28+MjYwMjIwMjYwMTE3MTYyNDc5NDMwMDExMDAxMDAxMDAwMDAwMDI0MzI5NzI1NDYxNDwvY2xhdmVBY2Nlc28+CiAgICAgICAgPGNvZERvYz4wMTwvY29kRG9jPgogICAgICAgIDxlc3RhYj4wMDE8L2VzdGFiPgogICAgICAgIDxwdG9FbWk+MDAxPC9wdG9FbWk+CiAgICAgICAgPHNlY3VlbmNpYWw+MDAwMDAwMDI0PC9zZWN1ZW5jaWFsPgogICAgICAgIDxkaXJNYXRyaXo+UVVJVE8gWSBBVi5BVEFIVUFMUEE8L2Rpck1hdHJpej4KICAgIDwvaW5mb1RyaWJ1dGFyaWE+CiAgICA8aW5mb0ZhY3R1cmE+CiAgICAgICAgPGZlY2hhRW1pc2lvbj4yNi8wMi8yMDI2PC9mZWNoYUVtaXNpb24+CiAgICAgICAgPGRpckVzdGFibGVjaW1pZW50bz5GQVVTVElOTyBCVVJHQTwvZGlyRXN0YWJsZWNpbWllbnRvPgogICAgICAgIDxvYmxpZ2Fkb0NvbnRhYmlsaWRhZD5OTzwvb2JsaWdhZG9Db250YWJpbGlkYWQ+CiAgICAgICAgPHRpcG9JZGVudGlmaWNhY2lvbkNvbXByYWRvcj4wNTwvdGlwb0lkZW50aWZpY2FjaW9uQ29tcHJhZG9yPgogICAgICAgIDxyYXpvblNvY2lhbENvbXByYWRvcj5RVUlOQ0hFIE1PUkFOIExVSVMgQU5EUkVTPC9yYXpvblNvY2lhbENvbXByYWRvcj4KICAgICAgICA8aWRlbnRpZmljYWNpb25Db21wcmFkb3I+MTAwMzg2NjE3MzwvaWRlbnRpZmljYWNpb25Db21wcmFkb3I+CiAgICAgICAgPGRpcmVjY2lvbkNvbXByYWRvcj5GQVVTVElOTyBCVVJHQTwvZGlyZWNjaW9uQ29tcHJhZG9yPgogICAgICAgIDx0b3RhbFNpbkltcHVlc3Rvcz4xMS4zPC90b3RhbFNpbkltcHVlc3Rvcz4KICAgICAgICA8dG90YWxEZXNjdWVudG8+MDwvdG90YWxEZXNjdWVudG8+CiAgICAgICAgPHRvdGFsQ29uSW1wdWVzdG9zPgogICAgICAgICAgICA8dG90YWxJbXB1ZXN0bz4KICAgICAgICAgICAgICAgIDxjb2RpZ28+MjwvY29kaWdvPgogICAgICAgICAgICAgICAgPGNvZGlnb1BvcmNlbnRhamU+NDwvY29kaWdvUG9yY2VudGFqZT4KICAgICAgICAgICAgICAgIDxiYXNlSW1wb25pYmxlPjExLjM8L2Jhc2VJbXBvbmlibGU+CiAgICAgICAgICAgICAgICA8dmFsb3I+MS43PC92YWxvcj4KICAgICAgICAgICAgPC90b3RhbEltcHVlc3RvPgogICAgICAgIDwvdG90YWxDb25JbXB1ZXN0b3M+CiAgICAgICAgPHByb3BpbmE+MDwvcHJvcGluYT4KICAgICAgICA8aW1wb3J0ZVRvdGFsPjEzPC9pbXBvcnRlVG90YWw+CiAgICAgICAgPG1vbmVkYT5ET0xBUjwvbW9uZWRhPgogICAgICAgIDxwYWdvcz4KICAgICAgICAgICAgPHBhZ28+CiAgICAgICAgICAgICAgICA8Zm9ybWFQYWdvPjIwPC9mb3JtYVBhZ28+CiAgICAgICAgICAgICAgICA8dG90YWw+MTM8L3RvdGFsPgogICAgICAgICAgICAgICAgPHBsYXpvPjE8L3BsYXpvPgogICAgICAgICAgICAgICAgPHVuaWRhZFRpZW1wbz5kaWFzPC91bmlkYWRUaWVtcG8+CiAgICAgICAgICAgIDwvcGFnbz4KICAgICAgICA8L3BhZ29zPgogICAgPC9pbmZvRmFjdHVyYT4KICAgIDxkZXRhbGxlcz4KICAgICAgICA8ZGV0YWxsZT4KICAgICAgICAgICAgPGNvZGlnb1ByaW5jaXBhbD42MDkzMzI4MzA0ODY8L2NvZGlnb1ByaW5jaXBhbD4KICAgICAgICAgICAgPGRlc2NyaXBjaW9uPkVMRiBMSVAgT0lMIENBTkRZIENPREVEPC9kZXNjcmlwY2lvbj4KICAgICAgICAgICAgPGNhbnRpZGFkPjE8L2NhbnRpZGFkPgogICAgICAgICAgICA8cHJlY2lvVW5pdGFyaW8+MTEuMzwvcHJlY2lvVW5pdGFyaW8+CiAgICAgICAgICAgIDxkZXNjdWVudG8+MDwvZGVzY3VlbnRvPgogICAgICAgICAgICA8cHJlY2lvVG90YWxTaW5JbXB1ZXN0bz4xMS4zPC9wcmVjaW9Ub3RhbFNpbkltcHVlc3RvPgogICAgICAgICAgICA8aW1wdWVzdG9zPgogICAgICAgICAgICAgICAgPGltcHVlc3RvPgogICAgICAgICAgICAgICAgICAgIDxjb2RpZ28+MjwvY29kaWdvPgogICAgICAgICAgICAgICAgICAgIDxjb2RpZ29Qb3JjZW50YWplPjQ8L2NvZGlnb1BvcmNlbnRhamU+CiAgICAgICAgICAgICAgICAgICAgPHRhcmlmYT4xNTwvdGFyaWZhPgogICAgICAgICAgICAgICAgICAgIDxiYXNlSW1wb25pYmxlPjExLjM8L2Jhc2VJbXBvbmlibGU+CiAgICAgICAgICAgICAgICAgICAgPHZhbG9yPjEuNzwvdmFsb3I+CiAgICAgICAgICAgICAgICA8L2ltcHVlc3RvPgogICAgICAgICAgICA8L2ltcHVlc3Rvcz4KICAgICAgICA8L2RldGFsbGU+CiAgICA8L2RldGFsbGVzPgo8ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczpldHNpPSJodHRwOi8vdXJpLmV0c2kub3JnLzAxOTAzL3YxLjMuMiMiIElkPSJTaWduYXR1cmU3ODcxNDkiPgo8ZHM6U2lnbmVkSW5mbyBJZD0iU2lnbmF0dXJlLVNpZ25lZEluZm84MzY2NDIiPgo8ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnL1RSLzIwMDEvUkVDLXhtbC1jMTRuLTIwMDEwMzE1Ii8+CjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz4KPGRzOlJlZmVyZW5jZSBJZD0iU2lnbmVkUHJvcGVydGllc0lEOTM5NDM4IiBUeXBlPSJodHRwOi8vdXJpLmV0c2kub3JnLzAxOTAzI1NpZ25lZFByb3BlcnRpZXMiIFVSST0iI1NpZ25hdHVyZTc4NzE0OS1TaWduZWRQcm9wZXJ0aWVzMTk5NDI1Ij4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+CjxkczpEaWdlc3RWYWx1ZT53enhwTjBVb2NSU1J1UTZVcDJaR3hkZUJzZ2c9PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjxkczpSZWZlcmVuY2UgVVJJPSIjQ2VydGlmaWNhdGUxNDMyNDQ3Ij4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+CjxkczpEaWdlc3RWYWx1ZT5iQUdLcEl0Q0xpV3BmQlZwWXRKdXdxMTZ6U289PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjxkczpSZWZlcmVuY2UgSWQ9IlJlZmVyZW5jZS1JRC0xNjE2NjciIFVSST0iI2NvbXByb2JhbnRlIj4KPGRzOlRyYW5zZm9ybXM+CjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPgo8L2RzOlRyYW5zZm9ybXM+CjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPgo8ZHM6RGlnZXN0VmFsdWU+QjlzYjM1aW5QWGg5bVR2ZUc3RmtlRGQzMlEwPTwvZHM6RGlnZXN0VmFsdWU+CjwvZHM6UmVmZXJlbmNlPgo8L2RzOlNpZ25lZEluZm8+CjxkczpTaWduYXR1cmVWYWx1ZSBJZD0iU2lnbmF0dXJlVmFsdWUxMDA3Njg5Ij4KZ2JORE96ZVVVbjdYcmI2WitqdFV5UmpOSUYwMlEvTlYvQlJaQlVWVitMdnM5OFNPbXRycVJHb3QwU1p1N3dTdFBVbWFiamJySTVvSQp5YWxVQkkrcWxSK3JSeU55SlRxeDdpUnVma280bnZuUUd3MFFYbHZFUElBN01kNnh2VUl5bTdVTWpCdnhqay8zWjFKbVU4V3RSTGt4CmdNTVV6TlBPbytDN2xJZnN5MGF4UUhmc2xzdGNlbnJXMmFOMjBSNXN3NjNzTktEQjNWQ2dISDZuS09oVUdEeHNnMUZyVUMxRjIxZW8KeW42MGlMTnZ4djg5UTgzanROcWN3S2hGb2I2Z1VSMlU4blBJaEk1SW9EWEhHL0VaWjkrY0pIYStWN1JSeTV3a2FySkRoaXBBT1VuSApwNWtCUlF3Q3dpNjlHL3RQcXFZZmhKRHQ4UHM2SnRyZGowK1pEQT09CjwvZHM6U2lnbmF0dXJlVmFsdWU+CjxkczpLZXlJbmZvIElkPSJDZXJ0aWZpY2F0ZTE0MzI0NDciPgo8ZHM6WDUwOURhdGE+CjxkczpYNTA5Q2VydGlmaWNhdGU+Ck1JSUtxekNDQ0pPZ0F3SUJBZ0lJSlhzMTB2empleVF3RFFZSktvWklodmNOQVFFTEJRQXdnWmt4Q3pBSkJnTlZCQVlUQWtWRE1SMHcKR3dZRFZRUUtEQlJUUlVOVlVrbFVXU0JFUVZSQklGTXVRUzRnTWpFd01DNEdBMVVFQ3d3blJVNVVTVVJCUkNCRVJTQkRSVkpVU1VaSgpRMEZEU1U5T0lFUkZJRWxPUms5U1RVRkRTVTlPTVRrd053WURWUVFERERCQlZWUlBVa2xFUVVRZ1JFVWdRMFZTVkVsR1NVTkJRMGxQClRpQlRWVUpEUVMweUlGTkZRMVZTU1ZSWklFUkJWRUV3SGhjTk1qVXdPREl4TVRnME5qVTFXaGNOTWpjd09ESXhNVGcwTmpVMVdqQ0IKckRFcU1DZ0dBMVVFQXd3aFIwbFRVMFZNSUZaQlRrVlRVMEVnUTBGQ1FWTkRRVTVIVHlCQlRsSkJUa2RQTVNBd0hnWURWUVFGRXhjeApOekUyTWpRM09UUXpMVEl4TURneU5URXpOVFkwTnpFd01DNEdBMVVFQ3d3blJVNVVTVVJCUkNCRVJTQkRSVkpVU1VaSlEwRkRTVTlPCklFUkZJRWxPUms5U1RVRkRTVTlPTVIwd0d3WURWUVFLREJSVFJVTlZVa2xVV1NCRVFWUkJJRk11UVM0Z01qRUxNQWtHQTFVRUJoTUMKUlVNd2dnRWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUUNaZ3BvMENxZ0tqQ0NIV1h3MVVvbGhuQUppWVliZgoyZ2dOUnV2YVdwVW12YlZkalI1aEI2REhXaWEwQWFpR0dkQkRJWXdyemViS013YmpZOEtVV3FBQ1Jmb0hhRG9MK2VGbGN0STNHV1YrCjRqNzVBaTB3aCtXbkQvV3FuMVJvTW1uYmtjdHdxSXRNem5zN2xBclUvNzg3RFRmNTdhQnBrVlB6NGp2UDYycEFIY2dKR0NTZmZnOFAKeUlibitIcFptWHV6LzhscHBuMllVSFN6citxemdDMkxwTWV6Kyt0TnRyMlg5MUtUenJwVXVjbzdob3FtQkNWNExtQnpOUlNudklpZwpaZXo3WjV4SXUyc3hPbzhYc2RGWnhRZTNPbVJiTkxhZURISEpGWTlKSnlRQjVNK3k4YXBLdlgreEdDekdJckF0SUlJeHRCaitmcXZ1CkV6YWVhTDFYQWdNQkFBR2pnZ1hnTUlJRjNEQU1CZ05WSFJNQkFmOEVBakFBTUI4R0ExVWRJd1FZTUJhQUZJeTZ5aEZYZUNXQUhXc0sKUzFXL2phNWkzYjJQTUZrR0NDc0dBUVVGQndFQkJFMHdTekJKQmdnckJnRUZCUWN3QVlZOWFIUjBjRG92TDI5amMzQm5keTV6WldOMQpjbWwwZVdSaGRHRXVibVYwTG1WakwyVnFZbU5oTDNCMVlteHBZM2RsWWk5emRHRjBkWE12YjJOemNEQWpCZ05WSFJFRUhEQWFnUmhuCmFYTnpaV3gyWVc1bGMzTmhZVUJuYldGcGJDNWpiMjB3Z2dFR0JnTlZIU0FFZ2Y0d2dmc3dXZ1lLS3dZQkJBR0NwbklDQnpCTU1Fb0cKQ0NzR0FRVUZCd0lDTUQ0ZVBBQkRBR1VBY2dCMEFHa0FaZ0JwQUdNQVlRQmtBRzhBSUFCa0FHVUFJQUJRQUdVQWNnQnpBRzhBYmdCaApBQ0FBVGdCaEFIUUFkUUJ5QUdFQWJEQ0JuQVlLS3dZQkJBR0NwbklDQVRDQmpUQ0JpZ1lJS3dZQkJRVUhBZ0VXZm1oMGRIQnpPaTh2CmQzZDNMbk5sWTNWeWFYUjVaR0YwWVM1dVpYUXVaV012ZDNBdFkyOXVkR1Z1ZEM5a2IzZHViRzloWkhNdlRtOXliV0YwYVhaaGN5OVEKWDJSbFgwTmxjblJwWm1sallXUnZjeTlRYjJ4cGRHbGpZWE1nWkdVZ1EyVnlkR2xtYVdOaFpHOGdVR1Z5YzI5dVlTQk9ZWFIxY21GcwpMbkJrWmpBZEJnTlZIU1VFRmpBVUJnZ3JCZ0VGQlFjREFnWUlLd1lCQlFVSEF3UXdnZ0lQQmdOVkhSOEVnZ0lHTUlJQ0FqQ0IzcUE2Cm9EaUdObWgwZEhBNkx5OWpjbXd4TG5ObFkzVnlhWFI1WkdGMFlTNXVaWFF1WldNdmMzVmlZMkV5WTNKc01TOWpjbXhtYVd4bExtTnkKYktLQm42U0JuRENCbVRFNU1EY0dBMVVFQXd3d1FWVlVUMUpKUkVGRUlFUkZJRU5GVWxSSlJrbERRVU5KVDA0Z1UxVkNRMEV0TWlCVApSVU5WVWtsVVdTQkVRVlJCTVRBd0xnWURWUVFMRENkRlRsUkpSRUZFSUVSRklFTkZVbFJKUmtsRFFVTkpUMDRnUkVVZ1NVNUdUMUpOClFVTkpUMDR4SFRBYkJnTlZCQW9NRkZORlExVlNTVlJaSUVSQlZFRWdVeTVCTGlBeU1Rc3dDUVlEVlFRR0V3SkZRekE4b0RxZ09JWTIKYUhSMGNEb3ZMMk55YkRJdWMyVmpkWEpwZEhsa1lYUmhMbTVsZEM1bFl5OXpkV0pqWVRKamNtd3lMMk55YkdacGJHVXVZM0pzTUlIZwpvSUhkb0lIYWhvSFhhSFIwY0hNNkx5OXdiM0owWVd3dGIzQmxjbUZrYjNJeUxuTmxZM1Z5YVhSNVpHRjBZUzV1WlhRdVpXTXZaV3BpClkyRXZjSFZpYkdsamQyVmlMM2RsWW1ScGMzUXZZMlZ5ZEdScGMzUS9ZMjFrUFdOeWJDWnBjM04xWlhJOVEwNDlRVlZVVDFKSlJFRkUKSUVSRklFTkZVbFJKUmtsRFFVTkpUMDRnVTFWQ1EwRXRNaUJUUlVOVlVrbFVXU0JFUVZSQkxFOVZQVVZPVkVsRVFVUWdSRVVnUTBWUwpWRWxHU1VOQlEwbFBUaUJFUlNCSlRrWlBVazFCUTBsUFRpeFBQVk5GUTFWU1NWUlpJRVJCVkVFZ1V5NUJMaUF5TEVNOVJVTXdIUVlEClZSME9CQllFRkZpZHR2TXFDbW91L2R4V3k5dHA5Ykl6OVhSZk1Dc0dBMVVkRUFRa01DS0FEekl3TWpVd09ESXhNVGcwTmpVMVdvRVAKTWpBeU56QTRNakV4T0RRMk5UVmFNQXNHQTFVZER3UUVBd0lGNERBYUJnb3JCZ0VFQVlLbWNnTUJCQXdNQ2pFM01UWXlORGM1TkRNdwpHQVlLS3dZQkJBR0NwbklEQ1FRS0RBaFRRVTRnVEZWSlV6QVJCZ29yQmdFRUFZS21jZ01pQkFNTUFTNHdMUVlLS3dZQkJBR0NwbklECkJ3UWZEQjFOUlVwSlFTQlpJRVZPVkZKRklFSlBURWxXUVZJZ1dTQlRWVU5TUlRBZUJnb3JCZ0VFQVlLbWNnTUNCQkFNRGtkSlUxTkYKVENCV1FVNUZVMU5CTUI4R0Npc0dBUVFCZ3FaeUF5QUVFUXdQTURFek1EQXhNREF3TURFeU56TTNNQkVHQ2lzR0FRUUJncVp5QXlNRQpBd3dCTGpBVEJnb3JCZ0VFQVlLbWNnTWhCQVVNQTFCR1dEQVhCZ29yQmdFRUFZS21jZ01NQkFrTUIwVkRWVUZFVDFJd0dnWUtLd1lCCkJBR0NwbklEQXdRTURBcERRVUpCVTBOQlRrZFBNQmtHQ2lzR0FRUUJncVp5QXg0RUN3d0pTVTVIUlU1SlJWSkJNQjBHQ2lzR0FRUUIKZ3FaeUF3c0VEd3dOTVRjeE5qSTBOemswTXpBd01UQVJCZ29yQmdFRUFZS21jZ01kQkFNTUFTNHdGd1lLS3dZQkJBR0NwbklEQkFRSgpEQWRCVGxKQlRrZFBNQm9HQ2lzR0FRUUJncVp5QXdnRURBd0tNRGs0TWprd01UWXdNekFOQmdrcWhraUc5dzBCQVFzRkFBT0NBZ0VBCkZhV3ZwTnpEbitybmNrM3g0SzlPZ0l2ZFIzS2NQSXpKU3ZDMHJGTWtIYTNYQ2xWV21wVFRLbEVoU01BcmlITkUzTkNBUlFDcFVtWGcKVUs3ZkIxdHg2MDlkbXV2WEd5ZU5Peng0QTlPOHRvcTZnUzNTVGljTkErMzdjUi9XM2RWMzRmWVRrS2NJYlZrdTJDMXNqV25vLzM3Lwo4V3hzRFYvWGlyeWc0cHl2bzJWY3cyRUdKZkNqZWQxTW5VVUZlSzF3cXN2M1NYWG5aZWJ5ZWlHSWZuTmFtNXpnNzl5cFBaZlRodEpJCjY0Smo2UkJkdVI4NUhYbDFVYk5TRXowd2ZDQzQ4YlVXR29oTDFOeWswNXhqQ0hJQmpMRU9GNU9tclRiTnpKYkNRcUxlSDh5MzU2TG4KZXF3Nk5EdlV1M04wUGNWRWE3L1ZJb3A5VTNtNmFxdG9NMmpGcGswMStGZGdCUjlTNTM1NVBaWFQ2ZlVjbVVuN3dpeUljY0pRblN4VwpCM05FU0x6ekMzT0hORlZwSk8xM3dPVVpORkdZRGlBZ2pRK3dLVm5udWJLVDVhQWp3c3cvOFhZQTNnYmJHQlZXSVYwZXRMTE8rOTg0CjhiUldjRU1lVHFNVUQzNy9WNU1JelVUU3FYZTh2SkZiWjVoTHhicjlWVGFqVENRSTdsY0haRTZwQ3VSWnVkMUZmYWRBWmtYNzFQL3IKM24yMUhBVWd4RytwRlpnUEJiWlRKa3RBUTN6bFI1RndXTmpTMXIwS2wweHA3QkJRK1dHWGNTNkZqeWRyZ05FTUxpbmV1ZGJ2RWFkbQoxTEZrOE1EcWVVNDVId2lQdDZzY3N2VjRPYnlRSzRLYzA2OHRaQXdickl4d3NsYWJYTEJwQ29YVERud2orbElkQnhGSHZubFBZUkU9CjwvZHM6WDUwOUNlcnRpZmljYXRlPgo8L2RzOlg1MDlEYXRhPgo8ZHM6S2V5VmFsdWU+CjxkczpSU0FLZXlWYWx1ZT4KPGRzOk1vZHVsdXM+Cm1ZS2FOQXFvQ293Z2gxbDhOVktKWVp3Q1ltR0czOW9JRFVicjJscVZKcjIxWFkwZVlRZWd4MW9tdEFHb2hoblFReUdNSzgzbXlqTUcKNDJQQ2xGcWdBa1g2QjJnNkMvbmhaWExTTnhsbGZ1SSsrUUl0TUlmbHB3LzFxcDlVYURKcDI1SExjS2lMVE01N081UUsxUCsvT3cwMworZTJnYVpGVDgrSTd6K3RxUUIzSUNSZ2tuMzRQRDhpRzUvaDZXWmw3cy8vSmFhWjltRkIwczYvcXM0QXRpNlRIcy92clRiYTlsL2RTCms4NjZWTG5LTzRhS3BnUWxlQzVnY3pVVXA3eUlvR1hzKzJlY1NMdHJNVHFQRjdIUldjVUh0enBrV3pTMm5neHh5UldQU1Nja0FlVFAKc3ZHcVNyMS9zUmdzeGlLd0xTQ0NNYlFZL242cjdoTTJubWk5Vnc9PQo8L2RzOk1vZHVsdXM+CjxkczpFeHBvbmVudD5BUUFCPC9kczpFeHBvbmVudD4KPC9kczpSU0FLZXlWYWx1ZT4KPC9kczpLZXlWYWx1ZT4KPC9kczpLZXlJbmZvPgo8ZHM6T2JqZWN0IElkPSJTaWduYXR1cmU3ODcxNDktT2JqZWN0Njc5MDYxIj48ZXRzaTpRdWFsaWZ5aW5nUHJvcGVydGllcyBUYXJnZXQ9IiNTaWduYXR1cmU3ODcxNDkiPjxldHNpOlNpZ25lZFByb3BlcnRpZXMgSWQ9IlNpZ25hdHVyZTc4NzE0OS1TaWduZWRQcm9wZXJ0aWVzMTk5NDI1Ij48ZXRzaTpTaWduZWRTaWduYXR1cmVQcm9wZXJ0aWVzPjxldHNpOlNpZ25pbmdUaW1lPjIwMjYtMDItMjZUMjE6MDc6MjgtMDU6MDA8L2V0c2k6U2lnbmluZ1RpbWU+PGV0c2k6U2lnbmluZ0NlcnRpZmljYXRlPjxldHNpOkNlcnQ+PGV0c2k6Q2VydERpZ2VzdD48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3NoYTEiLz48ZHM6RGlnZXN0VmFsdWU+cytnQXQ5R3o1UHhGajJuZHZpb3VlOVd0a1RRPTwvZHM6RGlnZXN0VmFsdWU+PC9ldHNpOkNlcnREaWdlc3Q+PGV0c2k6SXNzdWVyU2VyaWFsPjxkczpYNTA5SXNzdWVyTmFtZT5DTj1BVVRPUklEQUQgREUgQ0VSVElGSUNBQ0lPTiBTVUJDQS0yIFNFQ1VSSVRZIERBVEEsT1U9RU5USURBRCBERSBDRVJUSUZJQ0FDSU9OIERFIElORk9STUFDSU9OLE89U0VDVVJJVFkgREFUQSBTLkEuIDIsQz1FQzwvZHM6WDUwOUlzc3Vlck5hbWU+PGRzOlg1MDlTZXJpYWxOdW1iZXI+MjcwMDgxMTU4MTg0MDkxNTIzNjwvZHM6WDUwOVNlcmlhbE51bWJlcj48L2V0c2k6SXNzdWVyU2VyaWFsPjwvZXRzaTpDZXJ0PjwvZXRzaTpTaWduaW5nQ2VydGlmaWNhdGU+PC9ldHNpOlNpZ25lZFNpZ25hdHVyZVByb3BlcnRpZXM+PGV0c2k6U2lnbmVkRGF0YU9iamVjdFByb3BlcnRpZXM+PGV0c2k6RGF0YU9iamVjdEZvcm1hdCBPYmplY3RSZWZlcmVuY2U9IiNSZWZlcmVuY2UtSUQtMTYxNjY3Ij48ZXRzaTpEZXNjcmlwdGlvbj5jb250ZW5pZG8gY29tcHJvYmFudGU8L2V0c2k6RGVzY3JpcHRpb24+PGV0c2k6TWltZVR5cGU+dGV4dC94bWw8L2V0c2k6TWltZVR5cGU+PC9ldHNpOkRhdGFPYmplY3RGb3JtYXQ+PC9ldHNpOlNpZ25lZERhdGFPYmplY3RQcm9wZXJ0aWVzPjwvZXRzaTpTaWduZWRQcm9wZXJ0aWVzPjwvZXRzaTpRdWFsaWZ5aW5nUHJvcGVydGllcz48L2RzOk9iamVjdD48L2RzOlNpZ25hdHVyZT48L2ZhY3R1cmE+	2602202601171624794300110010010000000243297254614	DEVUELTA	NO AUTORIZADO	1	2	182	2026-02-26 21:07:30.722365	2026-02-26 21:07:30.722395
\.


--
-- Data for Name: outlets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.outlets (outlet_id, outlet_uuid, outlet_city, outlet_address, outlet_name, outlet_telf, outlet_status, enterprise_id, date_created, date_updated) FROM stdin;
1	b5c9efc5-b0ca-499f-89dd-f1b23c4983c4	OTAVALO - MATRIZ	QUITO Y ATAHUALPA	IZENSHY	0994501275	t	2	2025-09-15 23:31:01.321775	2025-09-15 23:31:42.949943
2	612b39ab-d40b-428e-99c9-a9bea046284e	OTAVALO - MATRIZ	QUITO Y ATAHUALPA	GILÚ	0982901603	t	1	2026-01-19 17:53:50.610557	2026-01-19 17:53:50.610557
\.


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.products (product_id, product_uuid, product_name, product_code, product_description, category_id, detail_id, date_created, date_updated) FROM stdin;
1	6574c871-8e72-440e-985d-ac3c9bc463df	MAYBELLINE - LUMIMATTE	LUMIMATE-MAY	BASE LUMIMATE	1	4	2025-09-15 16:27:44.383903	2025-09-15 16:27:44.383903
35	1ac1e3bc-f7e7-4c31-89e2-cd2b80aabec0	ELF LIP OIL CANDY CODED	609332830486	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	35	2025-11-14 18:45:04.886963	2025-11-14 18:45:04.886983
36	b9d52c26-10ac-43f8-aff4-739dc85bb471	ELF LIP OIL SUPER NEUTRAL	609332830493	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	36	2025-11-14 18:45:05.196001	2025-11-14 18:45:05.196076
37	9773230a-c276-4dcc-bfd3-2dc2d2c159f6	NYX FAT OIL MISED CALL	800897233921	NYX LIP OIL, HIDRATANTE CON APLICADOR	11	37	2025-11-14 18:45:05.380399	2025-11-14 18:45:05.380428
38	575570c3-0634-462e-8311-236b77435339	NYX FAT OIL FOLLOW BACK	800897234003	NYX LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	38	2025-11-14 18:45:05.563747	2025-11-14 18:45:05.56384
39	93552de4-b360-4d0a-a469-20f24c512727	NYX FAT OIL THATS CHIC	800897233945	NYX LIP OIL, HIDRATANTE CON APLICADOR	11	39	2025-11-14 18:45:05.743702	2025-11-14 18:45:05.743765
40	167a0abe-a020-4885-b64e-582eb02b34d0	NYX FAT OIL STATUS UPDATE	800897234034	NYX LIP OIL, HIDRATANTE CON APLICADOR	11	40	2025-11-14 18:45:05.909532	2025-11-14 18:45:05.909552
41	6e3723b4-ffd7-4c90-b575-2ddda1092f58	NYX FAT OIL NEWSPEED	800897233976	NYX LIP OIL, HIDRATANTE CON APLICADOR	11	41	2025-11-14 18:45:06.084753	2025-11-14 18:45:06.084819
42	98b9107c-8d4d-4294-9631-7c03ae08918a	NYX FAT OIL SUPERMODEL	800897233938	NYX LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	42	2025-11-14 18:45:06.269544	2025-11-14 18:45:06.269608
43	079c4870-b016-4e66-8d4e-5a5ae3ab99b7	MAYBELLINE LIFTER GLOSS AMBER	041554583915	MAYBELLINE LIP OIL + HYALURONIC ACID	11	43	2025-11-14 18:45:06.544817	2025-11-14 18:45:06.544844
44	3f87841a-7b4e-41e9-b209-f0d9b316b087	MAYBELLINE LIFTER GLOSS REEF 	041554583908	MAYBELLINE LIP OIL + HYALURONIC ACID	11	44	2025-11-14 18:45:06.83962	2025-11-14 18:45:06.839681
45	f82f7edc-fc96-4c77-85c6-1c2511750572	MAYBELLINE LIFTER GLOSS TOPAZ	041554583939	MAYBELLINE LIP OIL + HYALURONIC ACID	11	45	2025-11-14 18:45:07.084962	2025-11-14 18:45:07.085
46	4bdbd397-c423-4b0a-8002-edce004a30ff	MAYBELLINE LIFTER GLOSS BUBBLEGUM	041554085402	MAYBELLINE LIP OIL + HYALURONIC ACID	11	46	2025-11-14 18:45:07.269271	2025-11-14 18:45:07.269326
47	39c456a9-5ac6-4cf0-befe-fd05bbbd8061	MAYBELLINE LIFTER GLOSS SILK	041554583885	MAYBELLINE LIP OIL + HYALURONIC ACID	11	47	2025-11-14 18:45:07.447775	2025-11-14 18:45:07.447829
48	67b8838f-059a-4a65-baad-f4354b5a6077	MAYBELLINE LIFTER GLOSS HONEY	041554098570	MAYBELLINE LIP OIL + HYALURONIC ACID	11	48	2025-11-14 18:45:07.629694	2025-11-14 18:45:07.629803
49	38a7f651-e0a0-4705-9f7b-73480c50ddba	MAYBELLINE LIFTER GLOSS SWEETHEART	041554085396	MAYBELLINE LIP OIL + HYALURONIC ACID	11	49	2025-11-14 18:45:07.821316	2025-11-14 18:45:07.821354
50	df22a2ff-d2cb-4ec2-b3a3-f5153f5c97ea	MAYBELLINE LIFTER PLUMP HOTCHILI	041554081879	MAYBELLINE LIFTER PLUMP + MAXI-LIP	11	50	2025-11-14 18:45:08.003046	2025-11-14 18:45:08.003081
51	393c10e1-1bd4-4162-a3fe-ef0aee4abfbf	MAYBELLINE LIFTER PLUMP COCOAZING	041554081893	MAYBELLINE LIFTER PLUMP + MAXI-LIP	11	51	2025-11-14 18:45:08.180667	2025-11-14 18:45:08.180679
4	9192c092-8e53-4ca5-9b90-b172a46cd07b	ELF BASE HALO GLOW	ELFBASEHALOGLO05	ELF BASE HALO GLOW TONO 05	1	5	2025-09-15 17:36:48.518221	2025-09-30 19:21:41.927636
18	222ff319-0adb-4e0a-9b38-508eabaae15e	Camiseta Azul	CAM001	Camiseta manga corta azul	9	18	2025-09-30 19:32:14.804462	2025-09-30 19:32:14.804532
19	ec7880cb-cc07-4cb5-84f7-fc8b267fa285	PANTALON NEGRO	PAN123	Pantalón clásico negro	9	19	2025-09-30 19:32:14.86785	2025-11-14 18:29:10.705557
20	550955b7-e5b3-4882-aace-2f4cb21c6699	GOT2B GLUE MINI	052336341456	GEL PARA FIJAR CEJAS	10	20	2025-11-14 18:45:01.814293	2025-11-14 18:45:01.814339
21	9d71917b-f20b-4d0f-9a42-75093d98e64d	GOT2B GLUE FULL SIZE	052336331457	GEL PARA FIJAR CEJAS	10	21	2025-11-14 18:45:02.119899	2025-11-14 18:45:02.119958
22	3b8343fb-a163-4700-87e7-1992be2cc9e1	ELF LIP OIL MONEY MAUVE	609332820128	ELF LIP OIL, HIDRATANTE CON APLICADOR	11	22	2025-11-14 18:45:02.396921	2025-11-14 18:45:02.396981
23	a21e1730-7dda-415b-b72f-ac05eff5ac2f	ELF LIP OIL JAM SESSION	609332820098	ELF LIP OIL, HIDRATANTE CON APLICADOR	11	23	2025-11-14 18:45:02.602931	2025-11-14 18:45:02.602991
24	72472fe6-3744-4a5f-8921-70fcfabd6f11	ELF LIP OIL CHOCOLUXE	609332830523	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	24	2025-11-14 18:45:02.831889	2025-11-14 18:45:02.831952
25	7f86583c-752a-4649-b94e-0eb1c4aeca40	ELF LIP OIL DIVINE WINE	609332830516	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	25	2025-11-14 18:45:03.041134	2025-11-14 18:45:03.041192
26	0ac985b7-9b8e-4bbc-958b-a039f99472ab	ELF LIP OIL CORAL FIXATION	609332820074	ELF LIP OIL, HIDRATANTE CON APLICADOR	11	26	2025-11-14 18:45:03.240286	2025-11-14 18:45:03.240346
27	e72a2727-ecd1-4d78-840d-adc0d94e0cb1	ELF LIP OIL JELLY POP	609332854000	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	27	2025-11-14 18:45:03.434252	2025-11-14 18:45:03.434275
28	39007cf2-6180-49ff-8396-0bdb7aa9f638	ELF LIP OIL PRINCESS CUT	609332830509	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	28	2025-11-14 18:45:03.620641	2025-11-14 18:45:03.6207
29	7181af37-9d05-4631-bdc3-dc97b0a147c6	ELF LIP OIL PINK QUARTZ	609332820043	ELF LIP OIL, HIDRATANTE CON APLICADOR	11	29	2025-11-14 18:45:03.816186	2025-11-14 18:45:03.816262
30	6d16f571-a2d0-4d91-941f-6cccf7fa0811	ELF LIP OIL ITS GIVING GUAVA	609332820135	ELF LIP OIL, HIDRATANTE CON APLICADOR	11	30	2025-11-14 18:45:04.006812	2025-11-14 18:45:04.006848
31	341264de-0478-4fd4-b494-92a472a4e35a	ELF LIP OIL OPAL OGY YOUR	609332830479	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	31	2025-11-14 18:45:04.201977	2025-11-14 18:45:04.202037
32	61cbc963-b6d0-46c0-844e-b4fb3ca2b20d	ELF LIP OIL RED DELICIOUS	609332820081	ELF LIP OIL, HIDRATANTE CON APLICADOR	11	32	2025-11-14 18:45:04.380739	2025-11-14 18:45:04.380803
33	7ad72dfb-e63c-4ffd-98ff-9fce30795898	ELF LIP OIL CITRINE GLEAM	609332830455	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	33	2025-11-14 18:45:04.553079	2025-11-14 18:45:04.5531
34	6a89628c-14bb-488b-acb8-aa01471760ce	ELF LIP OIL CRYSTALL BALLER	609332830462	ELF LIP OIL, HIDRATANTE CON APLICADOR Y BRILLOS	11	34	2025-11-14 18:45:04.726106	2025-11-14 18:45:04.726126
52	eee97ba7-91e0-4214-aa02-fe2ff2ec86bd	MAYBELLINE LIFTER PLUMP PEACHFEVER	041554080711	MAYBELLINE LIFTER PLUMP + MAXI-LIP	11	52	2025-11-14 18:45:08.354887	2025-11-14 18:45:08.354976
53	2d6e1da0-6c53-4316-9293-76b23d50961c	MAYBELLINE LIFTER PLUMP HOTHONEY	041554080681	MAYBELLINE LIFTER PLUMP + MAXI-LIP	11	53	2025-11-14 18:45:08.551004	2025-11-14 18:45:08.551042
54	0f105111-cc97-4e57-8619-f3c18680b772	KIKO MILANO 3D HYDRA LIP GLOSS 35	8025272977388	KIKO MILANO LIP GLOSS TONO 35	11	54	2025-11-14 18:45:08.73046	2025-11-14 18:45:08.730476
55	21c04759-ee2a-41a2-9b83-b154f2f50d71	KIKO MILANO 3D HYDRA LIP GLOSS 05	8025272603911	KIKO MILANO LIP GLOSS TONO 05	11	55	2025-11-14 18:45:08.908837	2025-11-14 18:45:08.908867
56	a72496b9-19c6-408d-94d0-33f1e97887be	KIKO MILANO 3D HYDRA LIP GLOSS 33	8025272977364	KIKO MILANO LIP GLOSS TONO 33	11	56	2025-11-14 18:45:09.098647	2025-11-14 18:45:09.098668
57	45ab9f28-bbcf-4a6d-bbd6-b970059d0774	KIKO MILANO 3D HYDRA LIP GLOSS 31	8025272977241	KIKO MILANO LIP GLOSS TONO 31	11	57	2025-11-14 18:45:09.265007	2025-11-14 18:45:09.265023
58	b2713440-9c59-4a7d-8047-1c8c19c3bae4	KIKO MILANO 3D HYDRA LIP GLOSS 27	8025272604130	KIKO MILANO LIP GLOSS TONO 27	11	58	2025-11-14 18:45:09.439952	2025-11-14 18:45:09.439989
59	45cfd699-03d8-494f-94c2-bac9e1c98712	KIKO MILANO 3D HYDRA LIP GLOSS 26	8025272604123	KIKO MILANO LIP GLOSS TONO 26	11	59	2025-11-14 18:45:09.617016	2025-11-14 18:45:09.61703
60	c5e6d00d-8331-402e-ac8d-b8bc30635a6d	KIKO MILANO 3D HYDRA LIP GLOSS 01	8025272603874	KIKO MILANO LIP GLOSS TONO 01	11	60	2025-11-14 18:45:09.806842	2025-11-14 18:45:09.806882
61	b6946474-42bf-48d7-b31f-1cc6f1c2b573	ESSENCE SHINY LIPGLOSS POPPIN POMEGRANATE	4059729395177	ESSENCE SHINY GLOSS BRILLO LABIOS NO PEGAJOSO	11	61	2025-11-14 18:45:09.982161	2025-11-14 18:45:09.9822
62	61f2062c-99c0-4ab4-89e9-9648c5617ae6	ESSENCE SHINY LIPGLOSS LOVELY LITCHI	4059729394606	ESSENCE SHINY GLOSS BRILLO LABIOS NO PEGAJOSO	11	62	2025-11-14 18:45:10.177585	2025-11-14 18:45:10.177605
63	681157e0-961e-4de6-8849-f5dedf44d06b	ESSENCE SHINY LIPGLOSS WITTY WATERMELON	4059729394590	ESSENCE SHINY GLOSS BRILLO LABIOS NO PEGAJOSO	11	63	2025-11-14 18:45:10.345959	2025-11-14 18:45:10.345991
113	b5b3b048-4617-4cc6-9d80-ae47893cb332	LOREAL SKIN INK 100 WARM	071249700501	LOREAL SKIN INK 100	15	111	2025-11-14 18:45:17.880444	2025-11-14 18:45:17.880455
64	7385fd3a-2e09-47f8-ace6-f89893196494	ESSENCE SHINY LIPGLOSS PROUD PAPAYA	4059729394583	ESSENCE SHINY GLOSS BRILLO LABIOS NO PEGAJOSO	11	64	2025-11-14 18:45:10.541881	2025-11-14 18:45:10.541944
65	5765de43-04e5-4dd0-b472-cae7c2bfc2d4	ESSENCE SHINY LIPGLOSS CAKE MY DAY	4059729518613	ESSENCE SHINY GLOSS BRILLO LABIOS NO PEGAJOSO	11	65	2025-11-14 18:45:10.727082	2025-11-14 18:45:10.727122
66	c0786932-ae98-40f0-82b7-ef0b87d00072	ITALIA DELUXE PHAT LIP GLOW OIL AS IF	782353189037	ITALIA DELUXE LIP GLOW OIL	11	66	2025-11-14 18:45:10.8869	2025-11-14 18:45:10.886918
67	21fd4b6f-bea6-4d4e-af7d-1b89c0e76ea3	ITALIA DELUXE PHAT LIP GLOW OIL THATS HOT	782353189013	ITALIA DELUXE LIP GLOW OIL	11	67	2025-11-14 18:45:11.055672	2025-11-14 18:45:11.055726
68	c73f4f52-f5d4-41a7-992b-5070029f7fa7	ITALIA DELUXE PHAT LIP GLOW OIL BUZZIN	782353180968	ITALIA DELUXE LIP GLOW OIL	11	68	2025-11-14 18:45:11.254636	2025-11-14 18:45:11.254702
69	6f5d0ae4-18ca-416a-89d4-808828220778	MAXFINE LIP OIL 01	6932269200768	MAXFINE LIP OIL TONO 01	11	60	2025-11-14 18:45:11.379815	2025-11-14 18:45:11.379829
70	6816dffa-449d-4bbd-a0f7-e8d1222e1f99	MAXFINE LIP OIL 03	6932269200782	MAXFINE LIP OIL TONO 03	11	69	2025-11-14 18:45:11.572803	2025-11-14 18:45:11.572817
71	cbd71d41-7023-4215-8664-d13b57271a7f	MAXFINE LIP OIL 04	6932269200799	MAXFINE LIP OIL TONO 04	11	70	2025-11-14 18:45:11.86226	2025-11-14 18:45:11.862298
72	9fd98b7a-ff62-457b-a306-fd9fcbd8071c	MAXFINE LIP OIL 05	6932269200805	MAXFINE LIP OIL TONO 05	11	55	2025-11-14 18:45:12.050087	2025-11-14 18:45:12.050099
73	d6cdca6b-1112-4a19-91bd-9a5fb403dca6	MAXFINE LIP OIL 06	6932269200812	MAXFINE LIP OIL TONO 06	11	71	2025-11-14 18:45:12.389639	2025-11-14 18:45:12.389676
74	9f1b9406-072e-4c7d-b895-e06d8de4ae85	MAYBELLINE FIT ME BASE 130	041554433463	MAYBELLINE BASE TERMINADO MATTE	12	72	2025-11-14 18:45:12.63653	2025-11-14 18:45:12.636543
75	209d5d3d-87f1-4e1a-8779-8837d234433c	MAYBELLINE FIT ME BASE 222	041554466461	MAYBELLINE BASE TERMINADO MATTE	12	73	2025-11-14 18:45:12.839987	2025-11-14 18:45:12.840003
76	a61466ea-1f64-4b4e-8e54-d5889544ecd3	MAYBELLINE FIT ME BASE 128	041554438161	MAYBELLINE BASE TERMINADO MATTE	12	74	2025-11-14 18:45:13.036815	2025-11-14 18:45:13.03683
77	97fe33c9-57c5-4025-b6e1-b47b12a2e507	MAYBELLINE FIT ME BASE 220	041554433470	MAYBELLINE BASE TERMINADO MATTE	12	75	2025-11-14 18:45:13.22255	2025-11-14 18:45:13.222626
78	5dcc4f56-f81e-46bc-9ab8-0a18379ca568	MAYBELLINE FIT ME BASE 112	041554433425	MAYBELLINE BASE TERMINADO MATTE	12	76	2025-11-14 18:45:13.436922	2025-11-14 18:45:13.436975
79	137d6d5a-7911-475f-a730-f92d0c658d32	MAYBELLINE FIT ME BASE 125	041554433456	MAYBELLINE BASE TERMINADO MATTE	12	77	2025-11-14 18:45:13.593326	2025-11-14 18:45:13.593345
80	26e6a0ab-fcef-42e7-9a63-28a252164ca0	MAYBELLINE FIT ME BASE 118	041554539462	MAYBELLINE BASE TERMINADO MATTE	12	78	2025-11-14 18:45:13.73356	2025-11-14 18:45:13.733585
81	f2203fd5-f041-49af-b530-4426b6067a85	MAYBELLINE FIT ME BASE 230	041554433487	MAYBELLINE BASE TERMINADO MATTE	12	79	2025-11-14 18:45:13.87085	2025-11-14 18:45:13.870865
82	204f586c-bfa8-4d2c-96a6-c52b974e1786	LOREAL INFALLIBLE PRO-MATTE 104.5	071249361603	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	80	2025-11-14 18:45:14.015779	2025-11-14 18:45:14.015836
83	8468260e-4d4c-455a-bbc4-fff67ac9debb	LOREAL INFALLIBLE PRO-MATTE 103	071249293027	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	81	2025-11-14 18:45:14.252884	2025-11-14 18:45:14.252928
84	66baec71-f36c-47c3-b9d6-88791cff3b23	LOREAL INFALLIBLE PRO-MATTE 106	071249293058	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	82	2025-11-14 18:45:14.413463	2025-11-14 18:45:14.413519
85	d5225f08-b2a5-4e52-ad45-701352886895	LOREAL INFALLIBLE PRO-MATTE 102.5	071249412138	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	83	2025-11-14 18:45:14.57192	2025-11-14 18:45:14.571952
86	ae0aded6-4342-4f77-9a96-db4b62878ada	LOREAL INFALLIBLE PRO-MATTE 101	071249292990	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	84	2025-11-14 18:45:14.717593	2025-11-14 18:45:14.717628
87	e08f6cf8-abaa-4c3e-bcac-1ef226509305	LOREAL INFALLIBLE PRO-MATTE 104	071249293034	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	85	2025-11-14 18:45:14.881916	2025-11-14 18:45:14.88196
88	1144797b-ea85-4920-87b8-b8aa5f8b86b9	LOREAL INFALLIBLE PRO-MATTE 105	071249293041	LOREAL INFALLIBLE BASE TERMINADO MATTE	12	86	2025-11-14 18:45:15.034099	2025-11-14 18:45:15.034128
89	ef0dc6c6-4c14-48df-ab29-de804c8dd9f9	MAYBELLINE SUPER STAY LUMI MATTE 30H 112	041554094688	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	87	2025-11-14 18:45:15.232999	2025-11-14 18:45:15.233012
90	26e46c52-681f-45b4-b1fb-57871544a855	MAYBELLINE SUPER STAY LUMI MATTE 30H 118	041554094701	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	88	2025-11-14 18:45:15.380954	2025-11-14 18:45:15.381
91	cceddb76-13d2-40d7-8405-b2a93b1b8585	MAYBELLINE SUPER STAY LUMI MATTE 30H 220	041554094756	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	89	2025-11-14 18:45:15.522808	2025-11-14 18:45:15.522844
92	86cab242-f8a8-45b1-9890-5fded2db8ce4	MAYBELLINE SUPER STAY LUMI MATTE 30H 129	041554094749	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	90	2025-11-14 18:45:15.680978	2025-11-14 18:45:15.680994
93	02b5517a-1285-4d7c-809f-f3fc32756bcb	MAYBELLINE SUPER STAY LUMI MATTE 30H 120	041554094718	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	91	2025-11-14 18:45:15.827907	2025-11-14 18:45:15.827922
94	7dcbfc4d-4c79-4433-8fc9-28b44fd86025	MAYBELLINE SUPER STAY LUMI MATTE 30H 125	041554094725	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	92	2025-11-14 18:45:16.025695	2025-11-14 18:45:16.025726
95	f041e4c3-457d-4770-95e6-64510cd25ee3	MAYBELLINE SUPER STAY LUMI MATTE 30H 350	041554094824	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	93	2025-11-14 18:45:16.33631	2025-11-14 18:45:16.336339
96	6d5450f5-29ab-40a0-b8f2-ce780cc486e1	MAYBELLINE SUPER STAY LUMI MATTE 30H 115	041554094695	MAYBELLINE BASE TERMINADO SEMI-MATTE, MEDIA COBERTURA	13	94	2025-11-14 18:45:16.63097	2025-11-14 18:45:16.630986
97	9d9ba82d-359f-477a-a314-f9a88f78e95b	LOREAL INFALLIBLE FRESH WEAR 470	071249382257	LOREAL INFALLIBLE FRESH WEAR	13	95	2025-11-14 18:45:16.997893	2025-11-14 18:45:16.997916
98	470e27fb-c6ae-426b-8bcf-8dc41ec5b148	LOREAL INFALLIBLE FRESH WEAR 475	071249382264	LOREAL INFALLIBLE FRESH WEAR	13	96	2025-11-14 18:45:17.387419	2025-11-14 18:45:17.387458
99	1d969c4e-8b8c-4aed-bf72-49ff02fc73c5	LOREAL INFALLIBLE FRESH WEAR 480	071249382271	LOREAL INFALLIBLE FRESH WEAR	13	97	2025-11-14 18:45:17.598941	2025-11-14 18:45:17.598962
100	44bce547-594a-4a77-84c0-b8c4014fc3ee	LOREAL INFALLIBLE FRESH WEAR 481	071249408100	LOREAL INFALLIBLE FRESH WEAR	13	98	2025-11-14 18:45:17.645948	2025-11-14 18:45:17.645969
101	b6e42bea-f15a-4711-8c31-2491e3cd400f	LOREAL INFALLIBLE FRESH WEAR 482	071249408124	LOREAL INFALLIBLE FRESH WEAR	13	99	2025-11-14 18:45:17.676788	2025-11-14 18:45:17.676804
102	6125c2bc-ee9b-4494-b2ac-fb999bd4e31a	LOREAL INFALLIBLE FRESH WEAR 455	071249382226	LOREAL INFALLIBLE FRESH WEAR	13	100	2025-11-14 18:45:17.698093	2025-11-14 18:45:17.698111
103	39f245af-1b4c-4cc1-aca7-992471d36518	LOREAL INFALLIBLE FRESH WEAR 412	071249408063	LOREAL INFALLIBLE FRESH WEAR	13	101	2025-11-14 18:45:17.714574	2025-11-14 18:45:17.714585
104	7d3cd91e-2636-44e2-9500-846e12447569	ELF SOFT GLAM BASE 30	609332849471	ELF SOFT GLAM BASE HIDRATANTE	13	102	2025-11-14 18:45:17.731211	2025-11-14 18:45:17.731229
105	6a1ce009-7ba5-4657-a8d6-2d408dea0d7c	ELF SOFT GLAM BASE 32	609332849495	ELF SOFT GLAM BASE HIDRATANTE	13	103	2025-11-14 18:45:17.748492	2025-11-14 18:45:17.748516
106	0a682d24-632a-4682-b798-7b9d68529ffe	ELF SOFT GLAM BASE 12	609332849372	ELF SOFT GLAM BASE HIDRATANTE	13	104	2025-11-14 18:45:17.763778	2025-11-14 18:45:17.763789
107	f07a4f5c-d5e5-4fa2-b952-0e0ca2f7fa5c	LOREAL TRUE MATCH NUDE 4.5-5.5	071249648773	LOREAL TRUE MATCH	14	105	2025-11-14 18:45:17.782121	2025-11-14 18:45:17.782133
108	9a494d76-d777-4886-ad41-20070229ae0f	MAYBELLINE SUPER STAY SKIN TINT 322	041554083859	MAYBELLINE SUPER STAY SKIN TINT	14	106	2025-11-14 18:45:17.796814	2025-11-14 18:45:17.796824
109	ed8e9062-e47f-481b-8e49-85a564b3b5d9	MAYBELLINE DREAM RADIANT LIQUID 35	041554579109	MAYBELLINE DREAM RADIANT 35	13	107	2025-11-14 18:45:17.811519	2025-11-14 18:45:17.811532
110	a483c865-84ba-4213-9e8b-2d6c03565c19	LOREAL SKIN INK 125 WARM	071249700518	LOREAL SKIN INK 125	15	108	2025-11-14 18:45:17.831257	2025-11-14 18:45:17.831267
111	ce5c4ea9-9e00-4980-a3b5-2426e7626943	LOREAL SKIN INK 130 COOL	071249700488	LOREAL SKIN INK 130	15	109	2025-11-14 18:45:17.848151	2025-11-14 18:45:17.848188
112	0d83fd2f-325d-4b46-9d21-eda25de4551e	LOREAL SKIN INK 140 WARM	071249700525	LOREAL SKIN INK 140	15	110	2025-11-14 18:45:17.865054	2025-11-14 18:45:17.865064
114	01620abe-49bd-47bc-8506-114d0c1665a7	LOREAL SKIN INK 180 COOL	071249700532	LOREAL SKIN INK 180	15	112	2025-11-14 18:45:17.89709	2025-11-14 18:45:17.897101
115	712267eb-d7ed-46e7-8db4-425f4452afb9	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 128	041554541434	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 	13	113	2025-11-14 18:45:17.915297	2025-11-14 18:45:17.915308
116	f6e92de1-cb7f-436e-b641-0575c661a573	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 115	041554559408	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 	13	114	2025-11-14 18:45:17.931053	2025-11-14 18:45:17.931064
117	6e67c6b8-55b5-4890-8d01-b65ed0a51777	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 220	041554541458	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 	13	75	2025-11-14 18:45:17.943675	2025-11-14 18:45:17.943686
118	f88de86b-b46a-412f-ace2-b567f9322c49	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 120	041554541427	MAYBELLINE SUPER STAY ACTIVE WEAR 30H 	13	115	2025-11-14 18:45:17.959716	2025-11-14 18:45:17.959728
119	02246710-c085-4485-9ab0-4b490b6a3694	COVERGIRL FULL SPECTRUM MATTE AMBITION 2	3614227631328	COVERGIRL BASE MATTE	12	116	2025-11-14 18:45:17.976334	2025-11-14 18:45:17.976346
120	07bf5520-8ebc-430a-a0dc-f7a344b73643	REVLON COLOR STAY FULL COVER 390	309971335112	REVLON COLOR STAY FULL COVER 390	12	117	2025-11-14 18:45:17.992175	2025-11-14 18:45:17.992186
121	e00cac8b-bdee-4843-b2cb-8e87de4f09c3	REVLON COLOR STAY LONGWEAR MAKEUP 390	309970005054	REVLON COLOR STAY LONGWEAR MAKEUP 390	13	118	2025-11-14 18:45:18.008275	2025-11-14 18:45:18.008286
122	ec2785ff-8d1e-4620-872e-80633aa07aec	ELF CAMO HYDRATING CC CREAM 370 N	609332847835	ELF CAMO HYDRATING CC CREAM VITAMINA B5	13	119	2025-11-14 18:45:18.024139	2025-11-14 18:45:18.024149
123	0ee23329-d5c3-4a72-9bbc-e0adb1264fd9	CATRICE SOFT GLAM FILTER FLUID 020	4059729419309	CATRICE SOFT GLAM FILTER FLUID 020	13	120	2025-11-14 18:45:18.041016	2025-11-14 18:45:18.041028
124	568c6a40-db34-4204-adfe-572bc0be353b	CATRICE SOFT GLAM FILTER FLUID 010	4059729419293	CATRICE SOFT GLAM FILTER FLUID 010	13	121	2025-11-14 18:45:18.057545	2025-11-14 18:45:18.057555
125	60e661a3-505e-458b-a07d-28f2bcb518af	ELF HALO GLOW LIQUID FILTER 0.5 FAIR	609332835665	ELF HALO GLOW LIQUID FILTER 0.5	13	122	2025-11-14 18:45:18.074599	2025-11-14 18:45:18.074611
126	7c2e7c82-2ad5-4a74-bd9b-14e38d3ab9c3	ELF HALO GLOW LIQUID FILTER 0 FAIR	609332835658	ELF HALO GLOW LIQUID FILTER 0	13	123	2025-11-14 18:45:18.090779	2025-11-14 18:45:18.090789
127	69381478-6e1f-4ff6-86e7-ed88c266feed	ELF HALO GLOW LIQUID FILTER 3 LIGHT	609332821156	ELF HALO GLOW LIQUID FILTER 3	13	124	2025-11-14 18:45:18.105933	2025-11-14 18:45:18.105944
128	f6590449-f45e-450c-a35b-e7ffb769513b	ELF HALO GLOW LIQUID FILTER 5 MEDIUM	609332821170	ELF HALO GLOW LIQUID FILTER 5	13	125	2025-11-14 18:45:18.121628	2025-11-14 18:45:18.121638
129	f94d82ed-b4bb-48e2-90c4-db853625f3b6	ELF HALO GLOW LIQUID FILTER 1 FAIR	609332821132	ELF HALO GLOW LIQUID FILTER 1	13	126	2025-11-14 18:45:18.137217	2025-11-14 18:45:18.137233
130	58754937-c68f-483d-a53f-bd0d3b365af4	ELF HALO GLOW LIQUID FILTER 2 FAIR LIGHT	609332821149	ELF HALO GLOW LIQUID FILTER 2	13	127	2025-11-14 18:45:18.151838	2025-11-14 18:45:18.151849
131	09dee626-a0af-4f61-8dfc-3e4770459f02	ELF HALO GLOW SKIN TINT 4 LIGHT NEUTRAL	609332845077	ELF HALO GLOW SKIN TINT 4	14	128	2025-11-14 18:45:18.166461	2025-11-14 18:45:18.166471
132	9240a24d-144e-492a-9658-a50a202ca8a9	ELF HALO GLOW SKIN TINT 8 MEDIUM NEUTRAL	609332845114	ELF HALO GLOW SKIN TINT 8	14	129	2025-11-14 18:45:18.181132	2025-11-14 18:45:18.181142
133	ab8c1844-03cb-4a6a-8544-2e740ab83087	ELF HALO GLOW SKIN TINT 9 MEDIUM COOL	609332845121	ELF HALO GLOW SKIN TINT 9	14	130	2025-11-14 18:45:18.194795	2025-11-14 18:45:18.194805
134	05bff09d-eac1-4aff-8298-4748fc86786b	ELF HALO GLOW SKIN TINT 5 LIGHT WARM	609332845084	ELF HALO GLOW SKIN TINT 5	14	131	2025-11-14 18:45:18.208582	2025-11-14 18:45:18.208592
135	b6d828b6-b350-4220-9947-99f5846f8b55	ELF HALO GLOW SKIN TINT 6 LIGHT COOL	609332845091	ELF HALO GLOW SKIN TINT 6	14	132	2025-11-14 18:45:18.222716	2025-11-14 18:45:18.222726
136	ceae5a14-a695-46ca-b05a-19c02166f63a	ELF HALO GLOW SKIN TINT 7 MEDIUM WARM	609332845107	ELF HALO GLOW SKIN TINT 7	14	133	2025-11-14 18:45:18.237102	2025-11-14 18:45:18.237112
137	5bc75a3f-d6ac-4a8d-936f-43b1d14e3981	NYX BUTTER MELT GLAZE SKIN TINT 03	800897266370	NYX BUTTER MELT GLAZE SKIN TINT	14	134	2025-11-14 18:45:18.252209	2025-11-14 18:45:18.252223
138	2bd628cc-be83-4e9d-b714-9ff14f0bcd40	NYX BUTTER MELT GLAZE SKIN TINT 04	800897266387	NYX BUTTER MELT GLAZE SKIN TINT	14	135	2025-11-14 18:45:18.266309	2025-11-14 18:45:18.266319
139	1b977019-ff0b-405d-8dfa-e1b3a01b8c6c	ELF HYDRATING CAMO CONCEALER FAIR ROSE	609332848207	ELF HYDRATING CAMO CONCEALER	16	136	2025-11-14 18:45:18.282589	2025-11-14 18:45:18.282599
140	545815ed-8924-4e71-bcaa-0c170002ef15	ELF HYDRATING CAMO CONCEALER FAIR WARM	609332848214	ELF HYDRATING CAMO CONCEALER	16	137	2025-11-14 18:45:18.298469	2025-11-14 18:45:18.298482
141	b76a9ed4-c2fa-48b3-83c4-5ccd13b3369e	ELF HYDRATING CAMO CONCEALER MEDIUM WARM	609332848313	ELF HYDRATING CAMO CONCEALER	16	138	2025-11-14 18:45:18.317654	2025-11-14 18:45:18.317665
142	32cb8755-1f19-437a-b182-598a1fa45c38	ELF HYDRATING CAMO CONCEALER FAIR BEIGE	609332848221	ELF HYDRATING CAMO CONCEALER	16	139	2025-11-14 18:45:18.333915	2025-11-14 18:45:18.333926
143	0507ba8c-0504-4f11-b612-512912667aa2	ELF HYDRATING CAMO CONCEALER LIGHT PEACH	609332848252	ELF HYDRATING CAMO CONCEALER	16	140	2025-11-14 18:45:18.350024	2025-11-14 18:45:18.350059
144	724726ca-7727-48af-af39-6e05f98f860c	ELF HYDRATING CAMO CONCEALER LIGHT SAND	609332848245	ELF HYDRATING CAMO CONCEALER	16	141	2025-11-14 18:45:18.365376	2025-11-14 18:45:18.365382
145	69bfd410-b493-4755-a601-151b27252fba	ELF HYDRATING CAMO CONCEALER MEDIUM NEUTRAL	609332848306	ELF HYDRATING CAMO CONCEALER	16	142	2025-11-14 18:45:18.380411	2025-11-14 18:45:18.380418
146	6d6371c4-d5c7-42f7-a3a3-a39edd75e6e4	ELF HYDRATING CAMO CONCEALER DEEP OLIVE	609332848375	ELF HYDRATING CAMO CONCEALER	16	143	2025-11-14 18:45:18.396593	2025-11-14 18:45:18.396604
147	ba5b6744-5053-45ad-a364-307991335d8d	ELF HYDRATING CAMO CONCEALER MEDIUM GOLDEN	609332848320	ELF HYDRATING CAMO CONCEALER	16	144	2025-11-14 18:45:18.41561	2025-11-14 18:45:18.415618
148	b3b6fe8e-f96a-4600-ad7d-bf1a6edbe511	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 30	041554071511	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 30	16	145	2025-11-14 18:45:18.435023	2025-11-14 18:45:18.435033
149	250ae9fd-b05d-4abc-a548-529b9596dd58	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 20	041554071474	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 20	16	146	2025-11-14 18:45:18.453539	2025-11-14 18:45:18.453547
150	e5c5a66a-0ec5-4ced-b732-bc587e44b1cf	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 05	041554071429	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 05	16	55	2025-11-14 18:45:18.468946	2025-11-14 18:45:18.468955
151	40841e21-027e-41f9-9b76-8aafe589802b	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 15	041554071450	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 15	16	147	2025-11-14 18:45:18.487015	2025-11-14 18:45:18.487024
152	bbc78927-ee69-4ee3-bce6-118a9c0e35b6	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 25	041554071498	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 25	16	148	2025-11-14 18:45:18.504311	2025-11-14 18:45:18.504318
153	ad0ee3bc-5fbc-4627-b494-d8e58e0b73cc	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 22	041554071481	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 22	16	149	2025-11-14 18:45:18.518995	2025-11-14 18:45:18.519001
154	b374779b-77f8-4d48-9d86-3f776dcd00d4	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 18	041554071467	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 18	16	150	2025-11-14 18:45:18.533197	2025-11-14 18:45:18.533204
155	adef6878-314f-4614-94ec-1ad137c812aa	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 10	041554071436	MAYBELLINE SUPER STAY CONCEALER ACTIVE WEAR 10	16	151	2025-11-14 18:45:18.548769	2025-11-14 18:45:18.548777
156	5768618a-cc99-49dd-a482-02a5074765dd	MAYBELLINE ERASER INSTANT AGE REWIND 12H 110	041554259247	MAYBELLINE ERASER INSTANT AGE REWIND	16	152	2025-11-14 18:45:18.568826	2025-11-14 18:45:18.568834
157	412eca65-7090-4a38-9891-ab4adf3c1aaf	MAYBELLINE ERASER INSTANT AGE REWIND 12H 130	041554259261	MAYBELLINE ERASER INSTANT AGE REWIND	16	153	2025-11-14 18:45:18.585708	2025-11-14 18:45:18.585714
158	50c0abe1-ae52-4543-a22a-0c76318880a8	MAYBELLINE ERASER INSTANT AGE REWIND 12H 144	041554546811	MAYBELLINE ERASER INSTANT AGE REWIND	16	154	2025-11-14 18:45:18.599258	2025-11-14 18:45:18.599265
159	00bf4960-78da-4666-861c-db1ac87feca2	MAYBELLINE ERASER INSTANT AGE REWIND 12H 160	041554267204	MAYBELLINE ERASER INSTANT AGE REWIND	16	155	2025-11-14 18:45:18.615154	2025-11-14 18:45:18.615161
160	c23ae58b-b34f-4cd5-96a0-c4fa8f9167f3	MAYBELLINE ERASER INSTANT AGE REWIND 12H 150	041554267198	MAYBELLINE ERASER INSTANT AGE REWIND	16	156	2025-11-14 18:45:18.633458	2025-11-14 18:45:18.633465
161	ad5d9163-0eeb-4eff-86e3-2639a44bb75d	LOREAL INFALLIBLE CONCEALER 350 BISQUE	071249382448	LOREAL INFALLIBLE CONCEALER 350	16	157	2025-11-14 18:45:18.649854	2025-11-14 18:45:18.649861
162	f103fed0-c576-4b62-a4dc-87e7464f72a3	LOREAL INFALLIBLE CONCEALER 365 CASHEW	071249382479	LOREAL INFALLIBLE CONCEALER 365	16	158	2025-11-14 18:45:18.666465	2025-11-14 18:45:18.666471
163	dde30861-64d2-458f-ab80-4ab9a3d15de0	LOREAL INFALLIBLE CONCEALER 340 FAWN	071249382424	LOREAL INFALLIBLE CONCEALER 340	16	159	2025-11-14 18:45:18.683301	2025-11-14 18:45:18.683307
164	768d0cde-d5c2-4ab4-ac5d-928872829978	LOREAL INFALLIBLE CONCEALER 330 IVORY	071249382417	LOREAL INFALLIBLE CONCEALER 330	16	160	2025-11-14 18:45:18.699968	2025-11-14 18:45:18.699975
165	790126a8-8174-459f-8c1a-4f0a813de54e	LOREAL INFALLIBLE CONCEALER 375 LATTE	071249382493	LOREAL INFALLIBLE CONCEALER 375	16	161	2025-11-14 18:45:18.717197	2025-11-14 18:45:18.717205
166	a100f8d8-e4bd-4cf5-a825-feda76175a5f	LOREAL INFALLIBLE CONCEALER 370 BISCUIT	071249382486	LOREAL INFALLIBLE CONCEALER 370	16	162	2025-11-14 18:45:18.734507	2025-11-14 18:45:18.734513
167	2fa96640-d69b-4b16-a05b-aed6df8fdb06	LOREAL INFALLIBLE CONCEALER 320 PORCELAIN	071249382394	LOREAL INFALLIBLE CONCEALER 320	16	163	2025-11-14 18:45:18.751526	2025-11-14 18:45:18.751533
168	7c1ce4b5-25e9-442c-814f-05b358986d11	LOREAL INFALLIBLE CONCEALER 345 OATMEAL	071249382451	LOREAL INFALLIBLE CONCEALER 345	16	164	2025-11-14 18:45:18.766699	2025-11-14 18:45:18.766705
169	1184a608-2312-459d-b13b-7d2159beb538	LOREAL INFALLIBLE CONCEALER 390 CEDAR	071249382523	LOREAL INFALLIBLE CONCEALER 390	16	165	2025-11-14 18:45:18.781639	2025-11-14 18:45:18.781646
170	7bfa7655-3b01-4ed1-b238-492a6e9e659f	MILANI GILDEDMINI EYESHADOW 140 THE WINE DOWN 	717489436076	MILANI GILDEDMINI EYESHADOW 140 THE WINE DOWN 	17	166	2025-11-14 18:45:18.798562	2025-11-14 18:45:18.798568
171	ba8f410c-8540-4a0c-a38f-7f2effde292d	MILANI GILDEDMINI EYESHADOW 150 CALL ME OLD FASHIONED 	717489000031	MILANI GILDEDMINI EYESHADOW 150 CALL ME OLD FASHIONED	17	167	2025-11-14 18:45:18.813503	2025-11-14 18:45:18.813509
172	ed1d2964-8ace-4e3c-8482-3535fef87992	MILANI GILDEDMINI EYESHADOW 110 WISHKEY BUSSINES	717489436045	MILANI GILDEDMINI EYESHADOW 110	17	168	2025-11-14 18:45:18.827779	2025-11-14 18:45:18.827785
173	7f421a46-6e52-41da-a99e-2a18bc2cc297	MILANI GILDEDMINI EYESHADOW 120 ITS ALL ROSE	717489436052	MILANI GILDEDMINI EYESHADOW 120	17	169	2025-11-14 18:45:18.841869	2025-11-14 18:45:18.841876
174	cce835c5-d573-411d-ad38-da7f28fdecfe	ELF PERFECT 10 EYESHADOW EVERYDAY SMOKY	609332832787	ELF PERFECT 10 EYESHADOW 	17	170	2025-11-14 18:45:18.860686	2025-11-14 18:45:18.860692
175	c0f5bf39-ae54-4d09-8cec-fd40b516457b	ELF PERFECT 10 EYESHADOW NUDE ROSE GLOD	609332832770	ELF PERFECT 10 EYESHADOW 	17	171	2025-11-14 18:45:18.874694	2025-11-14 18:45:18.8747
176	579eb3c3-ad44-4c6a-9591-76343f967456	ELF CAMO LIQUID BRONZER Y CONTOUR 5 MEDIUM MOYEN	609332846135	ELF CAMO LIQUID BRONZER Y CONTOUR	18	172	2025-11-14 18:45:18.891824	2025-11-14 18:45:18.89183
177	ad79b6fb-2427-406c-a6fb-18f5d935f582	ELF CAMO LIQUID BRONZER Y CONTOUR 3 LIGHT CLAIR	609332846111	ELF CAMO LIQUID BRONZER Y CONTOUR	18	173	2025-11-14 18:45:18.905305	2025-11-14 18:45:18.905311
178	6a0030f0-cef7-44e0-ac59-b0616f5140b3	ELF CAMO LIQUID BRONZER Y CONTOUR 4 LIGHT MEDIUM	609332846128	ELF CAMO LIQUID BRONZER Y CONTOUR	18	174	2025-11-14 18:45:18.919238	2025-11-14 18:45:18.919245
179	4e08baa5-a9e1-45f9-8eec-8ae4269a577e	ELF POWDER COUNTOUR PALETTE PEEL 2	609332833203	ELF POWDER CONTOUR PALETTE	19	175	2025-11-14 18:45:18.935743	2025-11-14 18:45:18.935751
180	f42945ac-cc32-4f8f-963a-9864d801fe81	PHYSICIANS FORMULA HIGHLIGHT Y CONTOUR PALETTE 6810	044386068100	PHYSICIANS FORMULA HIGHLIGHT Y CONTOUR PALETTE	20	176	2025-11-14 18:45:18.952302	2025-11-14 18:45:18.95231
181	c0d336b0-fdfa-4b52-8d36-4cf6ba1f60e7	ELF BRONZING DROPS SKIN BRONZE 2	609332575196	ELF BRONZER LIQUIDO	21	177	2025-11-14 18:45:18.968969	2025-11-14 18:45:18.968977
182	ed869afe-8c87-4df8-92c3-39050321126c	WENT N WILD HIGHLIGHTING POWDER 319B	077802349712	WENT N WILD 	22	178	2025-11-14 18:45:18.985636	2025-11-14 18:45:18.985643
183	36cf41ee-e69f-462d-a278-278f5b5c918a	ELF MATTE BRONZER FOREVER SUN KISSED	609332830950	ELF BRONZER POWDER	21	179	2025-11-14 18:45:18.998541	2025-11-14 18:45:18.998547
184	c1c343a5-46f5-4b34-be2d-75404f4849aa	ELF MATTE BRONZER TAN OCLOCK	609332844964	ELF BRONZER POWDER	21	180	2025-11-14 18:45:19.012733	2025-11-14 18:45:19.012742
185	9471d386-4a9a-44ec-80dd-fb813f735632	COVERGIRL CLEAN FRESH 400	3614226675941	COVERGIRL CLEAN FINISHER	22	181	2025-11-14 18:45:19.026171	2025-11-14 18:45:19.026177
186	663afdfa-780a-4f52-9870-cf6c0b84670a	MAYBELLINE DREAM MATTE MOUSE LIGHT 1	041554507096	MAYBELLINE DREAM MATTE MOUSE	12	182	2025-11-14 18:45:19.04265	2025-11-14 18:45:19.042656
187	b2879de1-1dad-4c56-80d2-76d808dfda03	LOREAL INFALLIBLE BLUR FECTION 15	071249686690	LOREAL INFALLIBLE BLUR FECTION	23	183	2025-11-14 18:45:19.060666	2025-11-14 18:45:19.060672
188	a03c8d23-8dfd-46f7-8070-9e425a892e6d	LOREAL LUMI LE GLASS 610	071249693421	LOREAL LUMI LE GLASS	22	184	2025-11-14 18:45:19.075746	2025-11-14 18:45:19.075754
189	7b546564-dabc-4d61-bf92-d0aadde7bbf9	LOREAL LUMI LE GLASS 620	071249693438	LOREAL LUMI LE GLASS	22	185	2025-11-14 18:45:19.091759	2025-11-14 18:45:19.091767
190	77b48bad-75bc-4b63-bed6-99868c38b142	LOREAL INFALLIBLE FOUNDATION POWDER 125	071249627785	LOREAL INFALLIBLE FOUNDATION POWDER	24	186	2025-11-14 18:45:19.111149	2025-11-14 18:45:19.111157
191	5d9c4979-c7e3-49c9-a625-d420218fb0b6	LOREAL INFALLIBLE FOUNDATION POWDER 5	071249627730	LOREAL INFALLIBLE FOUNDATION POWDER	24	187	2025-11-14 18:45:19.125876	2025-11-14 18:45:19.125882
192	47ff789a-2015-49a7-8bed-fe6c405bf6bf	LOREAL INFALLIBLE FOUNDATION POWDER 180	071249627778	LOREAL INFALLIBLE FOUNDATION POWDER	24	188	2025-11-14 18:45:19.141124	2025-11-14 18:45:19.14113
193	5dbdb0f3-85c4-449c-8752-8ee1c7c8a8d2	ELF HALO GLOW SETTING POWDER MEDIUM	609332833913	ELF HALO GLOW SETTING POWDER	23	189	2025-11-14 18:45:19.15442	2025-11-14 18:45:19.154426
194	26333057-a698-43c9-b4e1-40dcf124928a	ELF HALO GLOW SETTING POWDER LIGHT PINK	609332833937	ELF HALO GLOW SETTING POWDER	23	190	2025-11-14 18:45:19.1678	2025-11-14 18:45:19.167806
195	48029df0-1aec-4fcc-93ae-3198fa22b75b	ESSENCE LOOSE FIXING POWDER 40 TAN	4059729259868	ESSENCE MY SKIN PERFECTOR	23	191	2025-11-14 18:45:19.180977	2025-11-14 18:45:19.180983
196	96ea3de1-13ff-4a72-83e6-2e4348544f32	AIRSPUN LOOSE FIXING FACE POWDER EXTRA COVERAGE	3616304008351	AIRSPUN 	23	192	2025-11-14 18:45:19.195758	2025-11-14 18:45:19.195763
197	a21cef0d-9567-45bc-8553-0f96fa5216d6	MAYBELLINE FIT ME BASE POLVO 220	041554433814	MAYBELLINE FIT ME BASE EN POLVO 220	24	75	2025-11-14 18:45:19.206449	2025-11-14 18:45:19.206454
198	c3f3e022-2f90-4e0d-a6a5-3af979d27005	MAYBELLINE FIT ME BASE POLVO 130	041554433807	MAYBELLINE FIT ME BASE EN POLVO 130	24	72	2025-11-14 18:45:19.217655	2025-11-14 18:45:19.217661
199	f250c00c-fb28-4cf5-8d58-7183b42030af	MAYBELLINE FIT ME BASE POLVO 245	041554553499	MAYBELLINE FIT ME BASE EN POLVO 245	24	193	2025-11-14 18:45:19.231103	2025-11-14 18:45:19.231109
200	e07dba3b-0429-4825-acc2-275954e5e262	MAYBELLINE FIT ME BASE POLVO 100	041554433746	MAYBELLINE FIT ME BASE EN POLVO 100	24	194	2025-11-14 18:45:19.245679	2025-11-14 18:45:19.245684
201	b83e88c4-c5d5-486f-8704-7c00e6ae7f1f	MAYBELLINE FIT ME LOOSE FINISHING POWDER	041554502039	MAYBELLINE FIT ME POLVO SUELTO	23	195	2025-11-14 18:45:19.259176	2025-11-14 18:45:19.259181
202	2609ade9-f384-4ba5-80ac-f3cfa845d490	NYX THE FACE GLUE GRIPPING PRIMER	800897266004	NYX THE FACE GLUE 	25	196	2025-11-14 18:45:19.275929	2025-11-14 18:45:19.275935
203	d1103a6a-0061-4afb-881c-79ed295c9d56	NYX THE MARSH MELLOW PRIMER	800897005078	NYX THE MARSH MELLOW PRIMER	25	196	2025-11-14 18:45:19.289042	2025-11-14 18:45:19.28905
204	65062e2a-4e1c-4018-841f-ed9a9992e1dc	MILANI ROSE POWDER BLUSH 08 TEA ROSE	717489950084	MILANI BLUSH EN POLVO 08 TEA ROSE	26	197	2025-11-14 18:45:19.307407	2025-11-14 18:45:19.307412
205	52f56127-922c-46b0-b829-d0539ed7b1ed	MILANI BAKED POWDER BLUSH 02 ROSE D ORO	717489821025	MILANI BLUSH EN POLVO 02 ROSE D ORO	26	198	2025-11-14 18:45:19.320154	2025-11-14 18:45:19.32016
206	3c8ab9e6-c268-4262-a599-b96e5dc79fc9	MILANI BAKED POWDER BLUSH 12 BELLA BELLINI	717489821124	MILANI BLUSH EN POLVO 12 BELLA BELLINI	26	199	2025-11-14 18:45:19.334175	2025-11-14 18:45:19.33418
207	83517ea4-aba6-4161-9ccf-6cd7f5d109cc	MILANI BAKED POWDER BLUSH 01 DOLCE PINK	717489821018	MILANI BLUSH EN POLVO 01 DOLCE PINK	26	200	2025-11-14 18:45:19.348064	2025-11-14 18:45:19.348069
208	7931b0e6-cbe2-4c3b-bca5-5b5e56f7c96e	MILANI BAKED POWDER BLUSH 14 PETAL PRIMAVERA	717489821148	MILANI BLUSH EN POLVO 14 PETAL PRIMAVERA	26	201	2025-11-14 18:45:19.361757	2025-11-14 18:45:19.361763
209	5539bc56-a1b4-4328-bb4f-75799fd1efcf	ELF HALO GLOW BEAUTY WAND BLUSH BERRY RADIANT	609332846999	ELF HALO GLOW BLUSH CON APLICADOR BERRY RADIANT	26	202	2025-11-14 18:45:19.375133	2025-11-14 18:45:19.375138
210	fd852694-f66e-41ac-a724-cf48ea93195a	ELF HALO GLOW BEAUTY WAND YOU GO COCOA	609332847019	ELF HALO GLOW BLUSH CON APLICADOR YOU GO COCOA	26	203	2025-11-14 18:45:19.389463	2025-11-14 18:45:19.389471
211	d84d16f3-73f7-42ef-806a-116bc82fc8db	ELF CAMO LIQUID BLUSH BOLD-FACED LILAC	609332846418	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO BOLD-FACED LILAC	26	204	2025-11-14 18:45:19.404949	2025-11-14 18:45:19.404957
212	dd82a62f-2aee-42c5-b41f-d1807c4a7b24	ELF CAMO LIQUID BLUSH CORAL CRUSH	609332824188	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO CORAL CRUSH	26	205	2025-11-14 18:45:19.421785	2025-11-14 18:45:19.421794
213	70928a00-2e87-46a1-8a75-93e4b363bfc0	ELF CAMO LIQUID BLUSH BRONZE BOMBSHELL	609332824225	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO BRONZE BOMBSHELL	26	206	2025-11-14 18:45:19.436826	2025-11-14 18:45:19.436831
214	1e93fa3a-26b0-4261-8fe3-ef5953fc56bd	ELF CAMO LIQUID BLUSH BERRY WELL	609332824218	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO BERRY WELL	26	207	2025-11-14 18:45:19.451032	2025-11-14 18:45:19.451038
215	1bfa43a9-36bb-4ca5-afbe-fbab8bcda346	ELF CAMO LIQUID BLUSH SUAVE MAUVE	609332824171	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO SUAVE MAUVE	26	208	2025-11-14 18:45:19.466398	2025-11-14 18:45:19.466403
216	17b33dea-146b-4473-9603-26d3d8e49ce7	ELF CAMO LIQUID BLUSH COMIN IN HOT PINK	609332824195	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO COMIN IN HOT PINK	26	209	2025-11-14 18:45:19.482415	2025-11-14 18:45:19.48242
217	5909ef73-5928-46e0-9d4b-6ddd75617c90	ELF CAMO LIQUID BLUSH PEACH PERFECT	609332824157	ELF CAMO LIQUID BLUSH RUBOR LIQUIDO PEACH PERFECT	26	210	2025-11-14 18:45:19.498053	2025-11-14 18:45:19.498059
218	0bf75608-b880-4b2f-8f1f-36b60bc658cf	NYX BUTTERMELT BLUSH 04 U KNOW BUTTA	800897257606	NYX BUTTERMELT BLUSH RUBOR EN POLVO 04 U KNOW BUTTA	26	211	2025-11-14 18:45:19.514919	2025-11-14 18:45:19.514925
219	261f11b3-a658-4171-9529-05d9bd3418d5	NYX BUTTERMELT BLUSH 07 BUTTA WITH TIME	800897257644	NYX BUTTERMELT BLUSH RUBOR EN POLVO 07 BUTTA WITH TIME	26	212	2025-11-14 18:45:19.530715	2025-11-14 18:45:19.530721
220	51577435-96a5-43e3-a8af-ad1a2f4a78a7	NYX BUTTERMELT BLUSH 08 GETTING BUTTA	800897257651	NYX BUTTERMELT BLUSH RUBOR EN POLVO 08 GETTING BUTTA	26	213	2025-11-14 18:45:19.54539	2025-11-14 18:45:19.545396
221	0144bea6-8f1a-4499-9c25-f83cc352a350	ELF MONOCHROMATIC MULTI-STICK DAZZLING PEONEY	609332813465	ELF RUBOR EN BARRA DAZZLING PEONY	26	214	2025-11-14 18:45:19.558936	2025-11-14 18:45:19.558941
222	bf46295e-8c55-4a6f-8538-2aad4a68f0eb	ELF MONOCHROMATIC MULTI-STICK GLISTENING PEACH	609332813267	ELF RUBOR EN BARRA GLISTENING PEACH	26	215	2025-11-14 18:45:19.573149	2025-11-14 18:45:19.573156
223	66b9abd9-5fab-46ce-a3ef-e755b26982bc	ELF MONOCHROMATIC MULTI-STICK SPARKLING ROSE	609332813250	ELF RUBOR EN BARRA SPARKLING ROSE	26	216	2025-11-14 18:45:19.587182	2025-11-14 18:45:19.587187
224	a2712312-f799-4009-b43a-23421af44083	ELF PUTTY BLUSH TAHITI	609332816213	ELF PUTTY BLUSH RUBOR EN CREMA TAHITI	26	217	2025-11-14 18:45:19.601503	2025-11-14 18:45:19.601509
225	ae9d50f2-20cc-430d-8c44-c4cdeb8e3666	ELF PUTTY BLUSH ISLA DEL SOL	609332815407	ELF PUTTY BLUSH ISLA DEL SOL	26	218	2025-11-14 18:45:19.617034	2025-11-14 18:45:19.61704
226	e0ec90fb-8d2e-4173-90aa-abd387750c4f	SPLASH VICTORIA SECRET	victoria3	SPLASH VICTORIA SECRET	27	219	2026-02-03 21:51:56.454972	2026-02-03 21:51:56.454996
227	9336690f-3785-4bbc-898b-2dd82b2d179a	VICTORIA SECRET SPLASH	VSSPLASH	VICTORIA SECRET SPLASH	27	219	2026-02-03 21:52:48.664133	2026-02-03 21:52:48.664151
228	f8f259c4-ed4d-46b9-8c59-8c67351fc30b	SPLASH VICTORIA SECRET	VSECRET001	SPLASH VICTORIA SECRET	27	219	2026-02-03 22:13:54.259604	2026-02-03 22:13:54.25963
229	9a4f9f29-cd8b-49c9-9bfb-91fd69a6c7e6	CREMA VICTORIA SECRET	VSCRET002	CREMA VICTORIA SECRET	27	219	2026-02-03 22:13:54.273129	2026-02-03 22:13:54.273145
230	1e17756f-8cce-4e15-9627-b40d21921221	E.L.F DELINEADORES	DELIELF	E.L.F DELINEADORES	27	219	2026-02-03 22:13:54.284734	2026-02-03 22:13:54.284751
231	f3f1b3e2-c946-4a45-ab69-9a0fc00ba394	KIKO MILANO 3D HYDRA GLOSS	KIKOGLOSS001	KIKO MILANO 3D HYDRA GLOSS	27	219	2026-02-03 22:13:54.297135	2026-02-03 22:13:54.297153
232	c6241bad-26e3-4fb4-9f7e-17aa46acba60	E.L.F TINT BULSH	TINTELF	E.L.F TINT BULSH	27	219	2026-02-03 22:13:54.312376	2026-02-03 22:13:54.312394
233	569f4728-8348-4848-8682-fdb724f04035	FIJADOR LOREAL	FIJLOREAL	FIJADOR LOREAL	27	219	2026-02-03 22:13:54.324616	2026-02-03 22:13:54.324644
234	ddc982fb-c628-4a2a-80a5-90d421f5dd27	MAYBELLINE BASE MATTE FIT ME 322	FITME322	MAYBELLINE BASE MATTE FIT ME 322	27	219	2026-02-03 22:13:54.337281	2026-02-03 22:13:54.337299
235	f8a8023f-7e1a-4c29-b068-4f10f6946786	LOREAL INFALLIBLE POWDER 120	LINFPOWDER120	LOREAL INFALLIBLE POWDER 120	27	219	2026-02-03 22:13:54.350961	2026-02-03 22:13:54.350979
236	dd1b46ac-1a1f-4875-a003-a48e8ab19279	LOREAL INFALLIBLE FRESH WEAR 410	LINFFRESH410	LOREAL INFALLIBLE FRESH WEAR 410	27	219	2026-02-03 22:13:54.36359	2026-02-03 22:13:54.363619
237	2aa49ebb-4092-4bdc-a65f-aa57fb503368	CAMISETA NEON	001125	CAMISETA ESTAMPADO ANIME NEON	28	220	2026-02-06 21:42:41.79647	2026-02-06 21:42:41.796564
238	ba688459-adf1-4a48-a62b-6cb9409e3653	MAYBELLINE SUPER STAY LUMI MATTE 30H 312	LUMMATTE312	MAYBELLINE SUPER STAY LUMI MATTE 30H 312	27	219	2026-02-13 13:41:48.602522	2026-02-13 13:41:48.602543
239	4ab2a16c-0328-4ba1-9864-46b0d09801a6	MAYBELLINE SUPER STAY TEDDY TINT 55 KNEEHIGH	041554089059	MAYBELLINE SUPER STAY TEDDY TINT 55 KNEEHIGH	27	219	2026-02-13 13:41:48.635169	2026-02-13 13:41:48.63519
240	8627d487-b05d-4cda-a96b-6d304ad30e62	VICTORIA SECRET SPLASH SHIMMER BARE VANILLA	VSSPLASHSHIMBAREV	VICTORIA SECRET SPLASH SHIMMER BARE VANILLA	27	219	2026-02-13 13:41:48.661444	2026-02-13 13:41:48.661464
241	f2e36105-5762-47a8-b027-53eceedde969	VICTORIA SECRET CREMA SHIMMER BARE VANILLA	VSSCREMASHIMBAREV	VICTORIA SECRET CREMA SHIMMER BARE VANILLA	27	219	2026-02-13 13:41:48.680542	2026-02-13 13:41:48.680577
242	ed37add3-f887-40d4-b2ae-aeade0f4dd12	MADAGASCAR CENTELLA TONE BRIGHTENING AMPOULE	CENTTNBRGAMP	MADAGASCAR CENTELLA TONE BRIGHTENING AMPOULE	27	219	2026-02-14 13:26:59.657272	2026-02-14 13:26:59.6573
243	ceddfa9c-68d4-4218-bb90-7a41b61d4072	LOREAL TRUE MATCHCH 2-3	LRLTEMCH23	LOREAL TRUE MATCHCH 2-3	27	219	2026-02-14 13:26:59.994766	2026-02-14 13:26:59.994821
\.


--
-- Data for Name: stock; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.stock (stock_product_id, stock_outlet_id, stock_quantity, stock_avalible, unit_price, pvp_price, stock_max, stock_min, apply_tax, tax_id) FROM stdin;
23	2	3	t	9	13	100	2	t	4
40	2	1	t	9	14	100	2	t	4
42	2	1	t	9	14	100	2	t	4
44	2	1	t	10	16	100	2	t	4
45	2	1	t	10	16	100	2	t	4
46	2	1	t	10	16	100	2	t	4
47	2	2	t	10	16	100	2	t	4
48	2	1	t	10	16	100	2	t	4
49	2	1	t	10	16	100	2	t	4
50	2	1	t	10	16.5	100	2	t	4
88	2	1	t	10	17	100	2	t	4
89	2	1	t	10	22	100	2	t	4
51	2	1	t	10	16.5	100	2	t	4
52	2	1	t	10	16.5	100	2	t	4
53	2	1	t	10	16.5	100	2	t	4
54	2	1	t	9	17	100	2	t	4
55	2	2	t	9	17	100	2	t	4
56	2	2	t	9	17	100	2	t	4
57	2	1	t	9	17	100	2	t	4
58	2	4	t	9	17	100	2	t	4
59	2	1	t	9	17	100	2	t	4
60	2	1	t	9	17	100	2	t	4
61	2	3	t	1.899999976158142	2.9000000953674316	100	2	t	4
62	2	2	t	1.899999976158142	2.9000000953674316	100	2	t	4
63	2	2	t	1.899999976158142	2.9000000953674316	100	2	t	4
64	2	1	t	1.899999976158142	2.9000000953674316	100	2	t	4
65	2	1	t	1.899999976158142	2.9000000953674316	100	2	t	4
66	2	1	t	2.299999952316284	4.800000190734863	100	2	t	4
67	2	1	t	2.299999952316284	4.800000190734863	100	2	t	4
68	2	1	t	2.299999952316284	4.800000190734863	100	2	t	4
69	2	3	t	1	3.299999952316284	100	2	t	4
70	2	3	t	1	3.299999952316284	100	2	t	4
71	2	2	t	1	3.299999952316284	100	2	t	4
72	2	3	t	1	3.299999952316284	100	2	t	4
90	2	1	t	10	22	100	2	t	4
91	2	1	t	10	22	100	2	t	4
92	2	1	t	10	22	100	2	t	4
95	2	1	t	10	22	100	2	t	4
96	2	1	t	10	22	100	2	t	4
74	2	5	t	13	19	100	2	t	4
76	2	5	t	13	19	100	2	t	4
75	2	4	t	13	19	100	2	t	4
94	2	5	t	10	22	100	2	t	4
78	2	1	t	13	19	100	2	t	4
97	2	1	t	10	21	100	2	t	4
98	2	1	t	10	21	100	2	t	4
99	2	2	t	10	21	100	2	t	4
100	2	2	t	10	21	100	2	t	4
101	2	1	t	10	21	100	2	t	4
102	2	1	t	10	21	100	2	t	4
103	2	1	t	10	21	100	2	t	4
104	2	1	t	10	15	100	2	t	4
105	2	1	t	10	15	100	2	t	4
106	2	1	t	10	15	100	2	t	4
107	2	1	t	10	21	100	2	t	4
108	2	1	t	10	21	100	2	t	4
109	2	1	t	10	15	100	2	t	4
110	2	2	t	14	21	100	2	t	4
111	2	2	t	14	21	100	2	t	4
112	2	1	t	14	21	100	2	t	4
113	2	1	t	14	21	100	2	t	4
114	2	2	t	14	21	100	2	t	4
115	2	1	t	13	20	100	2	t	4
116	2	1	t	13	20	100	2	t	4
117	2	1	t	13	20	100	2	t	4
118	2	1	t	13	20	100	2	t	4
119	2	1	t	10	15	100	2	t	4
120	2	1	t	7	10	100	2	t	4
121	2	1	t	7	11	100	2	t	4
122	2	1	t	10	21	100	2	t	4
123	2	1	t	11	15	100	2	t	4
124	2	1	t	11	15	100	2	t	4
125	2	1	t	13	19	100	2	t	4
126	2	1	t	13	19	100	2	t	4
127	2	2	t	13	19	100	2	t	4
41	2	0	t	9	14	100	2	t	4
77	2	0	t	13	19	100	2	t	4
93	2	0	t	10	22	100	2	t	4
43	2	0	t	10	16	100	2	t	4
73	2	2	t	1	3.299999952316284	100	2	t	4
128	2	1	t	13	19	100	2	t	4
129	2	1	t	13	19	100	2	t	4
131	2	1	t	13	21	100	2	t	4
132	2	1	t	13	21	100	2	t	4
133	2	1	t	13	21	100	2	t	4
134	2	1	t	13	21	100	2	t	4
135	2	1	t	13	21	100	2	t	4
136	2	1	t	13	21	100	2	t	4
137	2	2	t	13	21	100	2	t	4
138	2	1	t	13	21	100	2	t	4
139	2	2	t	7	13	100	2	t	4
140	2	1	t	7	13	100	2	t	4
142	2	2	t	7	13	100	2	t	4
143	2	2	t	7	13	100	2	t	4
144	2	1	t	7	13	100	2	t	4
145	2	1	t	7	13	100	2	t	4
146	2	1	t	7	13	100	2	t	4
147	2	1	t	7	13	100	2	t	4
148	2	1	t	10	16	100	2	t	4
149	2	1	t	10	16	100	2	t	4
150	2	1	t	10	16	100	2	t	4
151	2	1	t	10	16	100	2	t	4
152	2	1	t	10	16	100	2	t	4
153	2	1	t	10	16	100	2	t	4
154	2	1	t	10	16	100	2	t	4
155	2	1	t	10	16	100	2	t	4
156	2	1	t	10	15	100	2	t	4
157	2	1	t	10	15	100	2	t	4
158	2	1	t	10	15	100	2	t	4
159	2	1	t	10	15	100	2	t	4
160	2	1	t	10	15	100	2	t	4
161	2	1	t	10	17	100	2	t	4
162	2	2	t	10	17	100	2	t	4
163	2	2	t	10	17	100	2	t	4
164	2	3	t	10	17	100	2	t	4
165	2	1	t	10	17	100	2	t	4
166	2	2	t	10	17	100	2	t	4
167	2	1	t	10	17	100	2	t	4
168	2	2	t	10	17	100	2	t	4
169	2	1	t	10	17	100	2	t	4
170	2	1	t	9	14.5	100	2	t	4
171	2	1	t	9	14.5	100	2	t	4
172	2	1	t	9	14.5	100	2	t	4
173	2	1	t	9	14.5	100	2	t	4
174	2	1	t	9	15	100	2	t	4
175	2	1	t	9	15	100	2	t	4
176	2	1	t	9	15	100	2	t	4
177	2	1	t	9	15	100	2	t	4
179	2	1	t	9	16	100	2	t	4
180	2	1	t	10	20	100	2	t	4
181	2	1	t	2	5	100	2	t	4
182	2	1	t	6	13	100	2	t	4
183	2	1	t	8	12.5	100	2	t	4
184	2	1	t	8	12.5	100	2	t	4
185	2	1	t	7	7	100	2	t	4
186	2	1	t	7	13	100	2	t	4
187	2	1	t	10	16	100	2	t	4
188	2	1	t	10	21	100	2	t	4
189	2	1	t	10	21	100	2	t	4
190	2	2	t	10	19	100	2	t	4
191	2	1	t	10	19	100	2	t	4
192	2	1	t	10	19	100	2	t	4
193	2	1	t	10	15	100	2	t	4
194	2	1	t	10	15	100	2	t	4
195	2	1	t	3	6	100	2	t	4
196	2	1	t	7	18	100	2	t	4
197	2	1	t	7	15	100	2	t	4
198	2	1	t	7	15	100	2	t	4
199	2	1	t	7	15	100	2	t	4
200	2	1	t	7	15	100	2	t	4
201	2	1	t	10	17	100	2	t	4
202	2	4	t	8	17	100	2	t	4
203	2	1	t	8	16	100	2	t	4
204	2	1	t	7	15	100	2	t	4
205	2	1	t	9	15	100	2	t	4
206	2	2	t	9	15	100	2	t	4
207	2	1	t	9	15	100	2	t	4
208	2	1	t	9	15	100	2	t	4
209	2	1	t	8	13	100	2	t	4
141	2	1	t	7	13	100	2	t	4
20	2	19	t	5	8	100	2	t	4
80	2	1	t	13	19	100	2	t	4
81	2	2	t	13	19	100	2	t	4
82	2	1	t	10	17	100	2	t	4
83	2	2	t	10	17	100	2	t	4
18	2	1600	t	5.5	7.5	500	50	t	4
19	2	3200	t	10	15	300	20	f	1
84	2	1	t	10	17	100	2	t	4
85	2	1	t	10	17	100	2	t	4
86	2	1	t	10	17	100	2	t	4
1	2	51	t	11	13	10	100	t	4
4	2	19	t	15	20	13	1	t	4
21	2	30	t	8	14	100	2	t	4
22	2	1	t	9	13	100	2	t	4
24	2	1	t	9	13	100	2	t	4
25	2	1	t	9	13	100	2	t	4
26	2	1	t	9	13	100	2	t	4
27	2	1	t	9	13	100	2	t	4
28	2	1	t	9	13	100	2	t	4
29	2	1	t	9	13	100	2	t	4
30	2	1	t	9	13	100	2	t	4
31	2	1	t	9	13	100	2	t	4
32	2	1	t	9	13	100	2	t	4
33	2	1	t	9	13	100	2	t	4
34	2	1	t	9	13	100	2	t	4
79	2	0	t	13	19	100	2	t	4
87	2	1	t	10	17	100	2	t	4
37	2	1	t	9	14	100	2	t	4
38	2	1	t	9	14	100	2	t	4
39	2	1	t	9	14	100	2	t	4
178	2	1	t	9	15	100	2	t	4
210	2	1	t	8	13	100	2	t	4
211	2	1	t	8	13	100	2	t	4
22	1	1	t	9	13	100	2	t	4
23	1	1	t	9	13	100	2	t	4
24	1	1	t	9	13	100	2	t	4
25	1	1	t	9	13	100	2	t	4
26	1	1	t	9	13	100	2	t	4
27	1	1	t	9	13	100	2	t	4
28	1	1	t	9	13	100	2	t	4
29	1	1	t	9	13	100	2	t	4
30	1	1	t	9	13	100	2	t	4
31	1	1	t	9	13	100	2	t	4
32	1	1	t	9	13	100	2	t	4
33	1	1	t	9	13	100	2	t	4
34	1	1	t	9	13	100	2	t	4
36	1	1	t	9	13	100	2	t	4
37	1	1	t	9	14	100	2	t	4
38	1	1	t	9	14	100	2	t	4
39	1	1	t	9	14	100	2	t	4
40	1	1	t	9	14	100	2	t	4
41	1	1	t	9	14	100	2	t	4
42	1	1	t	9	14	100	2	t	4
44	1	1	t	10	16	100	2	t	4
45	1	1	t	10	16	100	2	t	4
46	1	1	t	10	16	100	2	t	4
47	1	2	t	10	16	100	2	t	4
48	1	1	t	10	16	100	2	t	4
49	1	1	t	10	16	100	2	t	4
50	1	1	t	10	16.5	100	2	t	4
51	1	1	t	10	16.5	100	2	t	4
52	1	1	t	10	16.5	100	2	t	4
53	1	1	t	10	16.5	100	2	t	4
54	1	1	t	9	17	100	2	t	4
55	1	2	t	9	17	100	2	t	4
56	1	2	t	9	17	100	2	t	4
57	1	1	t	9	17	100	2	t	4
58	1	4	t	9	17	100	2	t	4
59	1	1	t	9	17	100	2	t	4
60	1	1	t	9	17	100	2	t	4
61	1	3	t	1.899999976158142	2.9000000953674316	100	2	t	4
62	1	2	t	1.899999976158142	2.9000000953674316	100	2	t	4
63	1	2	t	1.899999976158142	2.9000000953674316	100	2	t	4
64	1	1	t	1.899999976158142	2.9000000953674316	100	2	t	4
65	1	1	t	1.899999976158142	2.9000000953674316	100	2	t	4
66	1	1	t	2.299999952316284	4.800000190734863	100	2	t	4
21	1	33	t	8	14	100	2	t	4
43	1	1	t	10	16	100	2	t	4
35	2	0	t	9	13	100	2	t	4
67	1	1	t	2.299999952316284	4.800000190734863	100	2	t	4
68	1	1	t	2.299999952316284	4.800000190734863	100	2	t	4
69	1	3	t	1	3.299999952316284	100	2	t	4
70	1	3	t	1	3.299999952316284	100	2	t	4
71	1	2	t	1	3.299999952316284	100	2	t	4
72	1	3	t	1	3.299999952316284	100	2	t	4
73	1	3	t	1	3.299999952316284	100	2	t	4
74	1	1	t	13	19	100	2	t	4
75	1	1	t	13	19	100	2	t	4
76	1	1	t	13	19	100	2	t	4
77	1	1	t	13	19	100	2	t	4
78	1	1	t	13	19	100	2	t	4
79	1	1	t	13	19	100	2	t	4
80	1	1	t	13	19	100	2	t	4
81	1	2	t	13	19	100	2	t	4
82	1	1	t	10	17	100	2	t	4
83	1	2	t	10	17	100	2	t	4
84	1	1	t	10	17	100	2	t	4
85	1	1	t	10	17	100	2	t	4
86	1	1	t	10	17	100	2	t	4
87	1	1	t	10	17	100	2	t	4
88	1	1	t	10	17	100	2	t	4
89	1	1	t	10	22	100	2	t	4
90	1	1	t	10	22	100	2	t	4
91	1	1	t	10	22	100	2	t	4
92	1	1	t	10	22	100	2	t	4
93	1	2	t	10	22	100	2	t	4
94	1	2	t	10	22	100	2	t	4
95	1	1	t	10	22	100	2	t	4
96	1	1	t	10	22	100	2	t	4
97	1	1	t	10	21	100	2	t	4
98	1	1	t	10	21	100	2	t	4
99	1	2	t	10	21	100	2	t	4
100	1	2	t	10	21	100	2	t	4
101	1	1	t	10	21	100	2	t	4
102	1	1	t	10	21	100	2	t	4
103	1	1	t	10	21	100	2	t	4
104	1	1	t	10	15	100	2	t	4
105	1	1	t	10	15	100	2	t	4
106	1	1	t	10	15	100	2	t	4
107	1	1	t	10	21	100	2	t	4
108	1	1	t	10	21	100	2	t	4
109	1	1	t	10	15	100	2	t	4
110	1	2	t	14	21	100	2	t	4
111	1	2	t	14	21	100	2	t	4
112	1	1	t	14	21	100	2	t	4
113	1	1	t	14	21	100	2	t	4
114	1	2	t	14	21	100	2	t	4
115	1	1	t	13	20	100	2	t	4
116	1	1	t	13	20	100	2	t	4
117	1	1	t	13	20	100	2	t	4
118	1	1	t	13	20	100	2	t	4
119	1	1	t	10	15	100	2	t	4
120	1	1	t	7	10	100	2	t	4
121	1	1	t	7	11	100	2	t	4
122	1	1	t	10	21	100	2	t	4
123	1	1	t	11	15	100	2	t	4
124	1	1	t	11	15	100	2	t	4
125	1	1	t	13	19	100	2	t	4
126	1	1	t	13	19	100	2	t	4
127	1	2	t	13	19	100	2	t	4
128	1	1	t	13	19	100	2	t	4
129	1	1	t	13	19	100	2	t	4
130	1	1	t	13	19	100	2	t	4
131	1	1	t	13	21	100	2	t	4
132	1	1	t	13	21	100	2	t	4
133	1	1	t	13	21	100	2	t	4
134	1	1	t	13	21	100	2	t	4
135	1	1	t	13	21	100	2	t	4
136	1	1	t	13	21	100	2	t	4
137	1	2	t	13	21	100	2	t	4
138	1	1	t	13	21	100	2	t	4
139	1	2	t	7	13	100	2	t	4
140	1	1	t	7	13	100	2	t	4
141	1	2	t	7	13	100	2	t	4
142	1	2	t	7	13	100	2	t	4
143	1	2	t	7	13	100	2	t	4
144	1	1	t	7	13	100	2	t	4
145	1	1	t	7	13	100	2	t	4
146	1	1	t	7	13	100	2	t	4
147	1	1	t	7	13	100	2	t	4
148	1	1	t	10	16	100	2	t	4
149	1	1	t	10	16	100	2	t	4
150	1	1	t	10	16	100	2	t	4
151	1	1	t	10	16	100	2	t	4
152	1	1	t	10	16	100	2	t	4
153	1	1	t	10	16	100	2	t	4
154	1	1	t	10	16	100	2	t	4
155	1	1	t	10	16	100	2	t	4
156	1	1	t	10	15	100	2	t	4
157	1	1	t	10	15	100	2	t	4
158	1	1	t	10	15	100	2	t	4
159	1	1	t	10	15	100	2	t	4
160	1	1	t	10	15	100	2	t	4
161	1	1	t	10	17	100	2	t	4
162	1	2	t	10	17	100	2	t	4
163	1	2	t	10	17	100	2	t	4
164	1	3	t	10	17	100	2	t	4
165	1	1	t	10	17	100	2	t	4
166	1	2	t	10	17	100	2	t	4
167	1	1	t	10	17	100	2	t	4
168	1	2	t	10	17	100	2	t	4
169	1	1	t	10	17	100	2	t	4
170	1	1	t	9	14.5	100	2	t	4
171	1	1	t	9	14.5	100	2	t	4
172	1	1	t	9	14.5	100	2	t	4
173	1	1	t	9	14.5	100	2	t	4
174	1	1	t	9	15	100	2	t	4
175	1	1	t	9	15	100	2	t	4
176	1	1	t	9	15	100	2	t	4
177	1	1	t	9	15	100	2	t	4
179	1	1	t	9	16	100	2	t	4
180	1	1	t	10	20	100	2	t	4
181	1	1	t	2	5	100	2	t	4
182	1	1	t	6	13	100	2	t	4
183	1	1	t	8	12.5	100	2	t	4
184	1	1	t	8	12.5	100	2	t	4
185	1	1	t	7	7	100	2	t	4
186	1	1	t	7	13	100	2	t	4
187	1	1	t	10	16	100	2	t	4
188	1	1	t	10	21	100	2	t	4
189	1	1	t	10	21	100	2	t	4
190	1	2	t	10	19	100	2	t	4
191	1	1	t	10	19	100	2	t	4
192	1	1	t	10	19	100	2	t	4
193	1	1	t	10	15	100	2	t	4
194	1	1	t	10	15	100	2	t	4
195	1	1	t	3	6	100	2	t	4
196	1	1	t	7	18	100	2	t	4
197	1	1	t	7	15	100	2	t	4
198	1	1	t	7	15	100	2	t	4
199	1	1	t	7	15	100	2	t	4
200	1	1	t	7	15	100	2	t	4
201	1	1	t	10	17	100	2	t	4
202	1	4	t	8	17	100	2	t	4
203	1	1	t	8	16	100	2	t	4
204	1	1	t	7	15	100	2	t	4
205	1	1	t	9	15	100	2	t	4
206	1	2	t	9	15	100	2	t	4
207	1	1	t	9	15	100	2	t	4
208	1	1	t	9	15	100	2	t	4
209	1	1	t	8	13	100	2	t	4
210	1	1	t	8	13	100	2	t	4
211	1	1	t	8	13	100	2	t	4
212	1	1	t	8	13	100	2	t	4
213	1	1	t	8	13	100	2	t	4
214	1	2	t	8	13	100	2	t	4
215	1	2	t	8	13	100	2	t	4
216	1	1	t	8	13	100	2	t	4
217	1	1	t	8	13	100	1	t	4
218	1	1	t	11	16	100	1	t	4
219	1	1	t	11	16	100	1	t	4
220	1	1	t	11	16	100	1	t	4
221	1	3	t	5	9	100	1	t	4
222	1	1	t	5	9	100	1	t	4
223	1	1	t	5	9	100	1	t	4
224	1	1	t	9	14	100	1	t	4
225	1	1	t	9	14	100	1	t	4
178	1	0	t	9	15	100	2	t	4
212	2	1	t	8	13	100	2	t	4
213	2	1	t	8	13	100	2	t	4
214	2	2	t	8	13	100	2	t	4
215	2	2	t	8	13	100	2	t	4
216	2	1	t	8	13	100	2	t	4
217	2	1	t	8	13	100	1	t	4
218	2	1	t	11	16	100	1	t	4
219	2	1	t	11	16	100	1	t	4
220	2	1	t	11	16	100	1	t	4
221	2	3	t	5	9	100	1	t	4
222	2	1	t	5	9	100	1	t	4
223	2	1	t	5	9	100	1	t	4
224	2	1	t	9	14	100	1	t	4
225	2	1	t	9	14	100	1	t	4
35	1	7	t	9	13	100	2	t	4
227	1	3	t	17	17	100	1	t	4
228	1	3	t	17	17	100	1	t	4
229	1	3	t	17	17	100	1	t	4
233	1	3	t	20	20	100	1	t	4
20	1	15	t	5	8	100	2	t	4
230	1	3	t	5.35	5.35	100	1	t	4
231	1	3	t	14.45	14.5	100	1	t	4
232	1	3	t	11.7	11.7	100	1	t	4
234	1	3	t	15.7	15.7	100	1	t	4
235	1	3	t	16.15	16.15	100	1	t	4
236	1	3	t	17.85	17.85	100	1	t	4
237	1	1	t	7	15	1	100	t	4
130	2	0	t	13	19	100	2	t	4
240	2	1	t	19	19	100	1	t	4
241	2	1	t	19	19	100	1	t	4
238	2	1	t	22	22	100	1	t	4
239	2	1	t	16	16	100	1	t	4
242	2	0	t	19.799999237060547	19.799999237060547	100	1	t	4
243	2	0	t	16.799999237060547	16.799999237060547	100	1	t	4
36	2	5	t	9	13	100	2	t	4
\.


--
-- Data for Name: taxes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.taxes (tax_id, tax_uuid, tax_code, tax_percentage, code_sri, tax_value, date_created, date_updated) FROM stdin;
1	a7d81dec-746d-434f-a13b-ba83dfc10939	0	0%	0	0	2025-09-27 00:24:58.176112	2025-09-27 00:24:58.176112
2	f0eac0fe-5b30-4306-b59a-f1fa9be52501	12	12%	2	12	2025-09-27 00:25:40.807127	2025-09-27 00:25:40.807127
3	795da2ab-6d3b-4038-b300-2e310cfe1e63	14	14%	3	14	2025-09-27 00:26:24.216096	2025-09-27 00:26:24.216096
4	b4b7ba42-e90a-442e-a3cb-500c3b5dca80	15	15%	4	15	2025-09-27 00:26:56.038143	2025-09-27 00:26:56.038143
5	fd7c0d3d-a696-401a-ada1-17d0d0bd2897	5	5%	5	5	2025-09-27 00:28:00.524296	2025-09-27 00:28:00.524296
6	06328efd-8907-4a4b-9b93-c122baddfed1	6	No Objeto de Impuesto	6	0	2025-09-27 00:28:51.299203	2025-09-27 00:28:51.299203
7	3d0cef8f-670f-4033-84c1-07f8773acaff	7	Exento de IVA	7	0	2025-09-27 00:29:24.65104	2025-09-27 00:29:24.65104
8	5c32a5db-5ddf-4898-84d7-e0f239235ec2	8	IVA diferenciado3	8	0	2025-09-27 00:31:16.188423	2025-09-27 00:31:16.188423
9	85871291-c80f-4a22-9989-063d50946be4	13	13%	10	0	2025-09-27 00:31:16.188423	2025-09-27 00:31:16.188423
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, user_uuid, user_name, user_lastname, user_password, user_gender, user_ci, user_ruc, user_status, date_created, date_updated, user_firstname, enterprise_id, user_rol) FROM stdin;
1	09dccaed-18bb-4cb5-bff4-59d95470f302	1003866173	QUINCHE MORAN 	$2y$10$cdI6005pPt1G79XJGeUAc.zP5nfxlkv7TMFnqfWaRC34i1ICfLY.e	M	1003866173	1003866173001	t	2025-07-30 17:25:12.440216	2025-09-22 22:45:31.739999	LUIS ANDRES	2	ADMIN
2	88eda0af-150c-4ec5-9c5a-213e402fbe3a	1716247943	CABASCANGO ANRANGO	$2y$10$cdI6005pPt1G79XJGeUAc.zP5nfxlkv7TMFnqfWaRC34i1ICfLY.e	F	1716247943	1716247943001	t	2025-09-01 00:51:03.743983	2025-09-22 22:45:31.739999	GISSEL VANESSA	1	ADMIN
3	a9e23ad9-4229-49e0-8b3b-0e71bbfb2bfd	1234567890	USER USER	$2y$10$cdI6005pPt1G79XJGeUAc.zP5nfxlkv7TMFnqfWaRC34i1ICfLY.e	F	1234567890	1234567890001	t	2025-09-19 12:31:17.372547	2025-09-22 22:45:31.739999	USER USER	2	USER
4	66d2391d-0dde-4ec2-9a99-64e7dd2321aa	1003866174	KINGCHE	$2a$10$T5qnvk0aN6G.oIXPGtSWoeOla4UPkcWlF1eFFC7.dylmQZSGCWZAO	M	1003866174	1003866174001	t	2025-09-25 00:46:38.802505	2025-09-25 00:46:38.802551	ANDY	2	USER
5	e49ba49f-e145-4da8-aa1b-0dff1e1160c5	1003866175	KINGCHE	$2a$10$Y4rjsfwWWti2BRTzhBOoheJ9nhMA2uQL5nsyFCtntsJvvSKvXFL0u	M	1003866175	1003866175001	t	2025-09-25 01:16:35.159328	2025-09-25 01:16:35.159354	LUCHO	2	USER
6	d216365b-08a4-4e0b-8b73-8c53336e0368	1003866176	AQ	$2a$10$oN.vpYbfNOyrMJ5FX8nnber/yhFJHrVLKB3nlORS/YFX1ImPTBFv6	F	1003866176	1003866176001	t	2025-09-25 01:45:39.160293	2025-09-25 01:45:39.160313	AQ	2	USER
7	8d753fdb-8668-496b-8aa5-ba8c9c650e13	1003866177	AQ	$2a$10$XsclWat2I8cpXDnO7/pdsujgRtjVQbf5Ul7dM6wBFsGA2pZJcbLT6	M	1003866177	1003866177001	t	2025-09-25 01:51:32.207404	2025-09-25 01:51:32.207425	AQ	2	USER
8	e1cc4238-53c7-4f58-b473-8b62759785a6	1003866178	AQ	$2a$10$aP8thNlwKs34pPtJgOOVCeu1yiV1Q2kam5bY/c7S/Rle2Qt2x9vdG	M	1003866178	1003866178001	t	2025-09-25 01:52:39.417349	2025-09-25 01:52:39.41738	AQ	2	USER
9	48d505f3-ec02-4f61-9efe-fcf7d5d88e8b	1003866179	PEREZ	$2a$10$JrYofjp07lrB/A2rl9VFg./M4a8glr/BxdW3HjmfMBFvhNXwd/JRu	M	1003866179	1003866179001	f	2025-09-25 08:59:18.83023	2025-09-25 18:15:43.726597	JUAN	2	ADMIN
10	4f93e849-7d1d-4504-a632-0db77b8ad2f9	1003866180	CUATRO	$2a$10$Osm9QkYaNz0frHTbgd3NOe3wSoUAr3ZZZC6EhhHifagWMkLVk903q	M	1003866181	1003866181001	f	2025-09-25 23:02:53.306458	2025-09-25 23:08:36.184649	CUATRO	2	USER
\.


--
-- Name: cash_register_cash_register_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cash_register_cash_register_id_seq', 21, true);


--
-- Name: cash_transactions_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cash_transactions_transaction_id_seq', 19, true);


--
-- Name: categories_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categories_category_id_seq', 28, true);


--
-- Name: clients_client_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.clients_client_id_seq', 11, true);


--
-- Name: details_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.details_detail_id_seq', 220, true);


--
-- Name: digital_certs_digital_cert_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.digital_certs_digital_cert_id_seq', 6, true);


--
-- Name: email_config_email_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.email_config_email_id_seq', 2, true);


--
-- Name: emitters_emitter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.emitters_emitter_id_seq', 2, true);


--
-- Name: enterprises_enterprise_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.enterprises_enterprise_id_seq', 2, true);


--
-- Name: invoice_detail_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.invoice_detail_detail_id_seq', 251, true);


--
-- Name: invoice_header_invoice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.invoice_header_invoice_id_seq', 182, true);


--
-- Name: invoice_temp_authorization_temp_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.invoice_temp_authorization_temp_id_seq', 4, true);


--
-- Name: outlets_outlet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.outlets_outlet_id_seq', 2, true);


--
-- Name: products_product_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.products_product_id_seq', 243, true);


--
-- Name: taxes_tax_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.taxes_tax_id_seq', 9, true);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 10, true);


--
-- Name: cash_register cash_register_cash_register_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_register
    ADD CONSTRAINT cash_register_cash_register_uuid_key UNIQUE (cash_register_uuid);


--
-- Name: cash_register cash_register_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_register
    ADD CONSTRAINT cash_register_pkey PRIMARY KEY (cash_register_id);


--
-- Name: cash_transactions cash_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_transactions
    ADD CONSTRAINT cash_transactions_pkey PRIMARY KEY (transaction_id);


--
-- Name: cash_transactions cash_transactions_transaction_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_transactions
    ADD CONSTRAINT cash_transactions_transaction_uuid_key UNIQUE (transaction_uuid);


--
-- Name: categories categories_category_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_category_uuid_key UNIQUE (category_uuid);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (category_id);


--
-- Name: clients clients_client_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_client_uuid_key UNIQUE (client_uuid);


--
-- Name: clients clients_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT clients_pkey PRIMARY KEY (client_id);


--
-- Name: details details_detail_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.details
    ADD CONSTRAINT details_detail_uuid_key UNIQUE (detail_uuid);


--
-- Name: details details_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.details
    ADD CONSTRAINT details_pkey PRIMARY KEY (detail_id);


--
-- Name: digital_certs digital_certs_digital_cert_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digital_certs
    ADD CONSTRAINT digital_certs_digital_cert_uuid_key UNIQUE (digital_cert_uuid);


--
-- Name: digital_certs digital_certs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digital_certs
    ADD CONSTRAINT digital_certs_pkey PRIMARY KEY (digital_cert_id);


--
-- Name: email_config email_config_email_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_config
    ADD CONSTRAINT email_config_email_uuid_key UNIQUE (email_uuid);


--
-- Name: email_config email_config_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_config
    ADD CONSTRAINT email_config_pkey PRIMARY KEY (email_id);


--
-- Name: emitters emitters_emitter_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emitters
    ADD CONSTRAINT emitters_emitter_uuid_key UNIQUE (emitter_uuid);


--
-- Name: emitters emitters_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emitters
    ADD CONSTRAINT emitters_pkey PRIMARY KEY (emitter_id);


--
-- Name: enterprises enterprises_enterprise_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enterprises
    ADD CONSTRAINT enterprises_enterprise_uuid_key UNIQUE (enterprise_uuid);


--
-- Name: enterprises enterprises_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.enterprises
    ADD CONSTRAINT enterprises_pkey PRIMARY KEY (enterprise_id);


--
-- Name: invoice_detail invoice_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_detail
    ADD CONSTRAINT invoice_detail_pkey PRIMARY KEY (detail_id);


--
-- Name: invoice_detail invoice_detail_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_detail
    ADD CONSTRAINT invoice_detail_uuid_key UNIQUE (detail_uuid);


--
-- Name: invoice_header invoice_header_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_header
    ADD CONSTRAINT invoice_header_pkey PRIMARY KEY (invoice_id);


--
-- Name: invoice_header invoice_header_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_header
    ADD CONSTRAINT invoice_header_uuid_key UNIQUE (invoice_uuid);


--
-- Name: invoice_temp_authorization invoice_temp_authorization_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_temp_authorization
    ADD CONSTRAINT invoice_temp_authorization_pkey PRIMARY KEY (temp_id);


--
-- Name: invoice_temp_authorization invoice_temp_authorization_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_temp_authorization
    ADD CONSTRAINT invoice_temp_authorization_uuid_key UNIQUE (temp_uuid);


--
-- Name: outlets outlets_outlet_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.outlets
    ADD CONSTRAINT outlets_outlet_uuid_key UNIQUE (outlet_uuid);


--
-- Name: outlets outlets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.outlets
    ADD CONSTRAINT outlets_pkey PRIMARY KEY (outlet_id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);


--
-- Name: products products_product_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_product_code_key UNIQUE (product_code);


--
-- Name: products products_product_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_product_uuid_key UNIQUE (product_uuid);


--
-- Name: stock stock_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT stock_pkey PRIMARY KEY (stock_product_id, stock_outlet_id);


--
-- Name: taxes taxes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.taxes
    ADD CONSTRAINT taxes_pkey PRIMARY KEY (tax_id);


--
-- Name: taxes taxes_tax_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.taxes
    ADD CONSTRAINT taxes_tax_code_key UNIQUE (tax_code);


--
-- Name: taxes taxes_tax_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.taxes
    ADD CONSTRAINT taxes_tax_uuid_key UNIQUE (tax_uuid);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users users_user_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_user_uuid_key UNIQUE (user_uuid);


--
-- Name: idx_cash_register_dates; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cash_register_dates ON public.cash_register USING btree (opening_date, closing_date);


--
-- Name: idx_cash_register_enterprise; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cash_register_enterprise ON public.cash_register USING btree (enterprise_id);


--
-- Name: idx_cash_register_outlet; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cash_register_outlet ON public.cash_register USING btree (outlet_id);


--
-- Name: idx_cash_register_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cash_register_status ON public.cash_register USING btree (status);


--
-- Name: idx_cash_register_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cash_register_user ON public.cash_register USING btree (user_id);


--
-- Name: idx_transaction_cash_register; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transaction_cash_register ON public.cash_transactions USING btree (cash_register_id);


--
-- Name: idx_transaction_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transaction_date ON public.cash_transactions USING btree (transaction_date);


--
-- Name: idx_transaction_invoice; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transaction_invoice ON public.cash_transactions USING btree (invoice_id);


--
-- Name: idx_transaction_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transaction_type ON public.cash_transactions USING btree (transaction_type);


--
-- Name: idx_transaction_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transaction_user ON public.cash_transactions USING btree (user_id);


--
-- Name: cash_register trigger_update_cash_register_date; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_cash_register_date BEFORE UPDATE ON public.cash_register FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: cash_transactions trigger_update_cash_transaction_date; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_cash_transaction_date BEFORE UPDATE ON public.cash_transactions FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: categories trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.categories FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: clients trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.clients FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: details trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.details FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: digital_certs trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.digital_certs FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: email_config trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.email_config FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: emitters trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.emitters FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: enterprises trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.enterprises FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: outlets trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.outlets FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: products trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.products FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: taxes trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.taxes FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: users trigger_update_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_date_updated BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: invoice_detail trigger_update_invoice_detail_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_invoice_detail_date_updated BEFORE UPDATE ON public.invoice_detail FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: invoice_header trigger_update_invoice_header_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_invoice_header_date_updated BEFORE UPDATE ON public.invoice_header FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: invoice_temp_authorization trigger_update_invoice_temp_authorization_date_updated; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_invoice_temp_authorization_date_updated BEFORE UPDATE ON public.invoice_temp_authorization FOR EACH ROW EXECUTE FUNCTION public.update_date_updated_column();


--
-- Name: cash_transactions trigger_update_register_totals; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_register_totals AFTER INSERT OR UPDATE ON public.cash_transactions FOR EACH ROW EXECUTE FUNCTION public.update_cash_register_totals();


--
-- Name: email_config email_config_enterprise_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.email_config
    ADD CONSTRAINT email_config_enterprise_id_fkey FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cash_register fk_cash_register_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_register
    ADD CONSTRAINT fk_cash_register_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cash_register fk_cash_register_outlet; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_register
    ADD CONSTRAINT fk_cash_register_outlet FOREIGN KEY (outlet_id) REFERENCES public.outlets(outlet_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cash_register fk_cash_register_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_register
    ADD CONSTRAINT fk_cash_register_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: clients fk_client_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clients
    ADD CONSTRAINT fk_client_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_detail fk_detail_invoice; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_detail
    ADD CONSTRAINT fk_detail_invoice FOREIGN KEY (invoice_id) REFERENCES public.invoice_header(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_detail fk_detail_stock; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_detail
    ADD CONSTRAINT fk_detail_stock FOREIGN KEY (stock_product_id, stock_outlet_id) REFERENCES public.stock(stock_product_id, stock_outlet_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: digital_certs fk_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.digital_certs
    ADD CONSTRAINT fk_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: emitters fk_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.emitters
    ADD CONSTRAINT fk_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invoice_header fk_invoice_customer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_header
    ADD CONSTRAINT fk_invoice_customer FOREIGN KEY (client_id) REFERENCES public.clients(client_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_header fk_invoice_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_header
    ADD CONSTRAINT fk_invoice_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_header fk_invoice_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_header
    ADD CONSTRAINT fk_invoice_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: outlets fk_outlet_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.outlets
    ADD CONSTRAINT fk_outlet_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: products fk_product_category; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES public.categories(category_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: products fk_product_detail; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fk_product_detail FOREIGN KEY (detail_id) REFERENCES public.details(detail_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: stock fk_stock_outlet; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT fk_stock_outlet FOREIGN KEY (stock_outlet_id) REFERENCES public.outlets(outlet_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: stock fk_stock_product; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT fk_stock_product FOREIGN KEY (stock_product_id) REFERENCES public.products(product_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: stock fk_stock_tax; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stock
    ADD CONSTRAINT fk_stock_tax FOREIGN KEY (tax_id) REFERENCES public.taxes(tax_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_temp_authorization fk_temp_auth_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_temp_authorization
    ADD CONSTRAINT fk_temp_auth_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: invoice_temp_authorization fk_temp_auth_invoice; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.invoice_temp_authorization
    ADD CONSTRAINT fk_temp_auth_invoice FOREIGN KEY (invoice_id) REFERENCES public.invoice_header(invoice_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cash_transactions fk_transaction_cash_register; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_transactions
    ADD CONSTRAINT fk_transaction_cash_register FOREIGN KEY (cash_register_id) REFERENCES public.cash_register(cash_register_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: cash_transactions fk_transaction_invoice; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_transactions
    ADD CONSTRAINT fk_transaction_invoice FOREIGN KEY (invoice_id) REFERENCES public.invoice_header(invoice_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: cash_transactions fk_transaction_user; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cash_transactions
    ADD CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: users fk_user_enterprise; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_user_enterprise FOREIGN KEY (enterprise_id) REFERENCES public.enterprises(enterprise_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict Icv8Z8emTp65cH9C6l19j5IRIpQyrRxMcCOpB7rd6Adq8o46yGuoaeDh381Tfef

