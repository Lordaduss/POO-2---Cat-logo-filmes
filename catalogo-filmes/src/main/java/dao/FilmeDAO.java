package dao;

import model.Filme;
import model.Genero;
import util.ConexaoDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class FilmeDAO {

    public void inserir(Filme filme) throws SQLException {
        String sql = "INSERT INTO filme (titulo, diretor, ano, nota, genero_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filme.getTitulo());
            ps.setString(2, filme.getDiretor());
            ps.setInt(3, filme.getAno());
            setNota(ps, 4, filme.getNota());
            ps.setInt(5, filme.getGenero().getId());
            ps.executeUpdate();
        }
    }

    public List<Filme> listar() throws SQLException {
        List<Filme> lista = new ArrayList<>();
        String sql = "SELECT f.id, f.titulo, f.diretor, f.ano, f.nota, "
                + "g.id AS genero_id, g.nome AS genero_nome "
                + "FROM filme f JOIN genero g ON f.genero_id = g.id "
                + "ORDER BY f.titulo";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double n = rs.getDouble("nota");
                Double nota = rs.wasNull() ? null : n;
                Genero genero = new Genero(rs.getInt("genero_id"), rs.getString("genero_nome"));
                lista.add(new Filme(rs.getInt("id"), rs.getString("titulo"),
                        rs.getString("diretor"), rs.getInt("ano"), nota, genero));
            }
        }
        return lista;
    }

    public void atualizar(Filme filme) throws SQLException {
        String sql = "UPDATE filme SET titulo = ?, diretor = ?, ano = ?, nota = ?, genero_id = ? WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, filme.getTitulo());
            ps.setString(2, filme.getDiretor());
            ps.setInt(3, filme.getAno());
            setNota(ps, 4, filme.getNota());
            ps.setInt(5, filme.getGenero().getId());
            ps.setInt(6, filme.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM filme WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private void setNota(PreparedStatement ps, int posicao, Double nota) throws SQLException {
        if (nota == null) {
            ps.setNull(posicao, Types.NUMERIC);
        } else {
            ps.setDouble(posicao, nota);
        }
    }
}
