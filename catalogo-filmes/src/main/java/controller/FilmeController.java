package controller;

import dao.FilmeDAO;
import dao.GeneroDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Filme;
import model.Genero;
import util.Alerta;

import java.sql.SQLException;

public class FilmeController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtDiretor;
    @FXML private TextField txtAno;
    @FXML private TextField txtNota;
    @FXML private ComboBox<Genero> cmbGenero;

    @FXML private TableView<Filme> tabela;
    @FXML private TableColumn<Filme, Integer> colId;
    @FXML private TableColumn<Filme, String> colTitulo;
    @FXML private TableColumn<Filme, String> colDiretor;
    @FXML private TableColumn<Filme, Integer> colAno;
    @FXML private TableColumn<Filme, Double> colNota;
    @FXML private TableColumn<Filme, String> colGenero;

    private final FilmeDAO filmeDAO = new FilmeDAO();
    private final GeneroDAO generoDAO = new GeneroDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDiretor.setCellValueFactory(new PropertyValueFactory<>("diretor"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("ano"));
        colNota.setCellValueFactory(new PropertyValueFactory<>("nota"));
        colGenero.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenero().getNome()));

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                preencherCampos(novo);
            }
        });

        carregarGeneros();
        carregarFilmes();
    }

    private void carregarGeneros() {
        try {
            cmbGenero.setItems(FXCollections.observableArrayList(generoDAO.listar()));
        } catch (SQLException e) {
            Alerta.erro("Erro ao carregar gêneros: " + e.getMessage());
        }
    }

    private void carregarFilmes() {
        try {
            tabela.setItems(FXCollections.observableArrayList(filmeDAO.listar()));
        } catch (SQLException e) {
            Alerta.erro("Erro ao carregar filmes: " + e.getMessage());
        }
    }

    private void preencherCampos(Filme f) {
        txtTitulo.setText(f.getTitulo());
        txtDiretor.setText(f.getDiretor() == null ? "" : f.getDiretor());
        txtAno.setText(String.valueOf(f.getAno()));
        txtNota.setText(f.getNota() == null ? "" : String.valueOf(f.getNota()));
        for (Genero g : cmbGenero.getItems()) {
            if (g.getId() == f.getGenero().getId()) {
                cmbGenero.setValue(g);
            }
        }
    }

    private Filme lerFormulario(int id) {
        String titulo = txtTitulo.getText().trim();
        String diretor = txtDiretor.getText().trim();
        Genero genero = cmbGenero.getValue();

        if (titulo.isEmpty() || genero == null) {
            Alerta.aviso("Título e gênero são obrigatórios.");
            return null;
        }

        int ano;
        try {
            ano = Integer.parseInt(txtAno.getText().trim());
        } catch (NumberFormatException e) {
            Alerta.aviso("Ano inválido.");
            return null;
        }

        Double nota = null;
        String textoNota = txtNota.getText().trim().replace(",", ".");
        if (!textoNota.isEmpty()) {
            try {
                nota = Double.parseDouble(textoNota);
            } catch (NumberFormatException e) {
                Alerta.aviso("Nota inválida.");
                return null;
            }
            if (nota < 0 || nota > 10) {
                Alerta.aviso("A nota deve estar entre 0 e 10.");
                return null;
            }
        }

        return new Filme(id, titulo, diretor, ano, nota, genero);
    }

    @FXML
    private void inserir() {
        Filme filme = lerFormulario(0);
        if (filme == null) {
            return;
        }
        try {
            filmeDAO.inserir(filme);
            limpar();
            carregarFilmes();
        } catch (SQLException e) {
            Alerta.erro("Erro ao inserir filme: " + e.getMessage());
        }
    }

    @FXML
    private void atualizar() {
        Filme selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alerta.aviso("Selecione um filme na tabela.");
            return;
        }
        Filme filme = lerFormulario(selecionado.getId());
        if (filme == null) {
            return;
        }
        try {
            filmeDAO.atualizar(filme);
            limpar();
            carregarFilmes();
        } catch (SQLException e) {
            Alerta.erro("Erro ao atualizar filme: " + e.getMessage());
        }
    }

    @FXML
    private void excluir() {
        Filme selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alerta.aviso("Selecione um filme na tabela.");
            return;
        }
        if (!Alerta.confirmar("Excluir o filme \"" + selecionado.getTitulo() + "\"?")) {
            return;
        }
        try {
            filmeDAO.excluir(selecionado.getId());
            limpar();
            carregarFilmes();
        } catch (SQLException e) {
            Alerta.erro("Erro ao excluir filme: " + e.getMessage());
        }
    }

    @FXML
    private void limpar() {
        txtTitulo.clear();
        txtDiretor.clear();
        txtAno.clear();
        txtNota.clear();
        cmbGenero.setValue(null);
        tabela.getSelectionModel().clearSelection();
    }
}
